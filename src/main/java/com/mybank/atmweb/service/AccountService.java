package com.mybank.atmweb.service;

import com.mybank.atmweb.application.query.UserQueryService;
import com.mybank.atmweb.domain.*;
import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.domain.account.AccountNumberGenerator;
import com.mybank.atmweb.domain.transaction.Transactions;
import com.mybank.atmweb.domain.user.User;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.domain.verification.VerificationResult;
import com.mybank.atmweb.dto.account.request.AccountOpenRequestDto;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.domain.account.AccountRepository;
import com.mybank.atmweb.repository.TransactionRepository;
import com.mybank.atmweb.service.transfer.model.OperationType;
import com.mybank.atmweb.service.verifier.AccountVerifier;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.mybank.atmweb.global.code.ErrorCode.BANK_INVALID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AccountService {
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final UserQueryService userQueryService;
    private final List<AccountVerifier> verifiers;
    private final TransactionRepository txRepo;

    public void createAccount(Long userId, AccountOpenRequestDto dto) {
        BankType bank;

        try {
            bank = BankType.valueOf(dto.getBank());
        } catch (IllegalArgumentException e) {
            throw new CustomException(BANK_INVALID);
        }

        User user = userQueryService.getByIdOrThrow(userId);

        String accountNumber = accountNumberGenerator.generate(bank.getPrefix());
        Account account = Account.builder()
                .owner(user)
                .accountName(dto.getAccountName())
                .bank(bank)
                .accountNumber(accountNumber)
                .balance(0L)
                .status(AccountStatus.ACTIVE)
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        accountRepository.save(account);
    }

    public VerificationResult verifyAccount(AccountVerifyRequestDto dto) {
        AccountVerifier verifier = verifiers.stream()
                .filter(v -> v.supports(dto.getBank()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No verifier found for bank: " + dto.getBank()));
        return verifier.doVerify(dto);
    }

    public void refundOutboundIfNeeded(String idempotencyKey, String fromAccountNumber, Long amount, String reason) {
        boolean alreadyRefunded = txRepo.existsByIdempotencyKeyAndOperationType(idempotencyKey, OperationType.REFUND);
        if (alreadyRefunded) {
            return;
        }

        // 계좌 락 + 잔액 복구
        Account from = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        long before = from.getBalance();
        from.deposit(amount);
        long after = from.getBalance();

        Transactions refund = Transactions.builder()
                .account(from)
                .operationType(OperationType.REFUND)
                .amount(amount)
                .balanceBefore(before)
                .balanceAfter(after)
                .memo("[보상] " + reason)
                .transactionStatus(TransactionStatus.COMPLETED)
                .idempotencyKey(idempotencyKey)
                .build();

        txRepo.save(refund);
    }
}

package com.mybank.atmweb.service;

import com.mybank.atmweb.application.query.AccountQueryService;
import com.mybank.atmweb.application.command.TransactionCommandService;
import com.mybank.atmweb.application.query.UserQueryService;
import com.mybank.atmweb.domain.*;
import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.domain.account.AccountNumberGenerator;
import com.mybank.atmweb.domain.user.User;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.domain.verification.VerificationResult;
import com.mybank.atmweb.dto.account.request.AccountOpenRequestDto;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.domain.account.AccountRepository;
import com.mybank.atmweb.repository.UserRepository;
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
public class AccountService {
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountQueryService accountQueryService;
    private final TransactionCommandService transactionCommandService;
    private final UserQueryService userQueryService;
    private final List<AccountVerifier> verifiers;

    public void createAccount(Long userId, AccountOpenRequestDto dto) {
        BankType bankType;

        try {
            bankType = BankType.valueOf(dto.getBank());
        } catch (IllegalArgumentException e) {
            throw new CustomException(BANK_INVALID);
        }

        User user = userQueryService.getByIdOrThrow(userId);

        String accountNumber = accountNumberGenerator.generate(bankType.getPrefix());
        Account account = Account.builder()
                .owner(user)
                .accountName(dto.getAccountName())
                .bank(bankType)
                .accountNumber(accountNumber)
                .balance(0L)
                .status(AccountStatus.ACTIVE)
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        accountRepository.save(account);
    }

    @Transactional
    public void handleDepositWithdraw(TransferDto dto, Long userId) {
        TransferType type = dto.getType();
        String accountNumber = dto.getAccountNumber();
        Long amount = dto.getAmount();

        Account account = accountQueryService.findAccountByAccountNumberAndUserId(accountNumber, userId);

        long before = account.getBalance();
        if (type == TransferType.DEPOSIT) {
            account.deposit(amount);
        } else if (type == TransferType.WITHDRAW) {
            account.withdraw(amount);
        }
        long after = account.getBalance();

        transactionCommandService.recordTransaction(account, before, after, dto);
    }

    public VerificationResult verifyAccount(AccountVerifyRequestDto dto) {
        AccountVerifier verifier = verifiers.stream()
                .filter(v -> v.supports(dto.getBank()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No verifier found for bank: " + dto.getBank()));
        return verifier.doVerify(dto);
    }
}

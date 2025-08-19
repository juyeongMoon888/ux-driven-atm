package com.mybank.atmweb.service;

import com.mybank.atmweb.application.AccountQueryService;
import com.mybank.atmweb.application.TransactionQueryService;
import com.mybank.atmweb.domain.*;
import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.domain.account.AccountNumberGenerator;
import com.mybank.atmweb.domain.transaction.Transaction;
import com.mybank.atmweb.domain.user.User;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.dto.account.request.AccountOpenRequestDto;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.AccountRepository;
import com.mybank.atmweb.repository.TransactionRepository;
import com.mybank.atmweb.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.mybank.atmweb.global.code.ErrorCode.BANK_INVALID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountService {
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountQueryService accountQueryService;
    private final TransactionQueryService transactionQueryService;

    public void createAccount(Long userId, AccountOpenRequestDto dto) {
        BankType bankType;

        try {
            bankType = BankType.valueOf(dto.getBank());
        } catch (IllegalArgumentException e) {
            throw new CustomException(BANK_INVALID);
        }

        User user = findUserByIdOrThrow(userId);

        String accountNumber = accountNumberGenerator.generate(bankType.getPrefix());
        Account account = Account.builder()
                .owner(user)
                .accountName(dto.getAccountName())
                .bank(bankType)
                .accountNumber(accountNumber)
                .balance(0L)
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();
        accountRepository.save(account);
    }

    public User findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    //리팩터링 할것
    public List<AccountSummaryDto> getAccountSummariesByUserId(Long userId) {
        Set<Account> accounts = accountRepository.findByOwner_Id(userId);

        return accounts.stream()
                .map(acc -> new AccountSummaryDto(
                        acc.getBank(),
                        acc.getAccountNumber(),
                        acc.getBalance()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void handleDepositWithdraw(TransferDto dto, Long userId) {
        TransferType type = dto.getType();
        String accountNumber = dto.getAccountNumber();
        Long amount = dto.getAmount();
        String memo = dto.getMemo();

        Account account = accountQueryService.findAccountByAccountNumberAndUserId(accountNumber, userId);

        long before = account.getBalance();
        if (type == TransferType.DEPOSIT) {
            account.deposit(amount);
        } else if (type == TransferType.WITHDRAW) {
            account.withdraw(amount);
        }
        long after = account.getBalance();

        recordTransaction(account, before, after, dto);
    }

    private void recordTransaction(Account account,
                                   Long balanceBefore,
                                   Long balanceAfter,
                                   TransferDto dto) {
        Transaction tx = new Transaction(account, dto.getType(), dto.getAmount(), balanceBefore, balanceAfter, dto.getMemo());

        transactionRepository.save(tx);
    }

    @Transactional
    public String updateTransactionMemo(Long transactionId, Long userId, MemoUpdateRequest memoRequest) {
        String memo = memoRequest.getMemo();
        Transaction tx = transactionQueryService.getTransactionOrThrow(transactionId, userId);
        tx.setMemo(memo);
        return tx.getAccount().getAccountNumber();
    }
}

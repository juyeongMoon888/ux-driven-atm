package com.mybank.atmweb.service;

import com.mybank.atmweb.domain.*;
import com.mybank.atmweb.dto.*;
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

import java.time.LocalDateTime;
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

    public void createAccount(Long userId, AccountRequestDto dto) {
        BankType bankType;

        try {
            bankType = BankType.valueOf(dto.getBank());

        } catch (IllegalArgumentException e) {
            log.error("[BANK_INVALID] dto.getBank() = {}, BankType enum과 매핑 실패", dto.getBank());
            throw new CustomException(BANK_INVALID);
        }

        User user = findUserByIdOrThrow(userId);

        Account account = Account.builder()
                //계좌 소유자 정보 연결
                .owner(user)
                .accountName(dto.getAccountName())
                .bank(bankType)
                .accountNumber(generateAccountNumber(bankType.getPrefix()))
                .balance(0L)
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        Account saved = accountRepository.save(account);
        log.info("✅ 저장된 계좌 ID: {}", saved.getId());
    }

    public String generateAccountNumber(String prefix) {
        String randomDigits = String.format("%08d", new Random().nextInt(100_100_100));
        return prefix + randomDigits;
    }

    public User findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

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
    public void updateBalance(TransferDto dto, Long userId) {
        TransferType type = dto.getType();
        String accountNumber = dto.getAccountNumber();
        Long amount = dto.getAmount();
        String memo = dto.getMemo();

        Account account = findAccountByAccountNumberAndUserId(accountNumber, userId);

        long before = account.getBalance();
        if (type == TransferType.DEPOSIT) {
            account.deposit(amount);
        } else if (type == TransferType.WITHDRAW) {
            account.withdraw(amount);
        }
        long after = account.getBalance();

        recordTransaction(account, before, after, dto);
    }

    public Account findAccountByAccountNumberAndUserId(String accountNumber, Long userId) {
        return accountRepository.findByAccountNumberAndOwner_Id(accountNumber, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND_FOR_USER));
    }

    private void recordTransaction(Account account,
                                   Long balanceBefore,
                                   Long balanceAfter,
                                   TransferDto dto) {
        Transaction tx = new Transaction(account, dto.getType(), dto.getAmount(), balanceBefore, balanceAfter, dto.getMemo());

        transactionRepository.save(tx);
    }

    public List<TransactionSummaryDto> getTransactionByAccountId(String accountNumber, Long userId) {
        Set<Transaction> transactions = transactionRepository.findByAccount_AccountNumberAndAccount_Owner_IdOrderByCreatedAtDesc(accountNumber, userId);

        return transactions.stream()
                .map(tx -> new TransactionSummaryDto(
                        tx.getId(),
                        tx.getCreatedAt(),
                        tx.getTransfer(),
                        tx.getAmount(),
                        tx.getMemo()
                ))
                .collect(Collectors.toList());
    }

    public TransactionDetailSummaryDto getHistoryDetail(Long transactionId, Long userId) {
        Transaction tx = getTransactionDetailOrThrow(transactionId, userId);

        return new TransactionDetailSummaryDto(
                tx.getCreatedAt(),
                tx.getTransfer(),
                tx.getAmount(),
                tx.getBalanceAfter(),
                tx.getMemo()
        );
    }

    public Transaction getTransactionDetailOrThrow(Long transactionId, Long userId) {
        return transactionRepository.findByIdAndAccount_Owner_Id(transactionId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_DETAIL_NOT_FOUND));
    }

    @Transactional
    public String updateTransactionMemo(Long transactionId, Long userId, MemoUpdateRequest memoRequest) {
        String memo = memoRequest.getMemo();

        Transaction tx = transactionRepository.findByIdAndAccount_Owner_Id(transactionId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        tx.setMemo(memo);

        return tx.getAccount().getAccountNumber();
    }
}

package com.mybank.atmweb.service;

import com.mybank.atmweb.domain.AccountStatus;
import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.domain.Idempotency;
import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.domain.account.AccountRepository;
import com.mybank.atmweb.domain.transaction.Transactions;
import com.mybank.atmweb.dto.TransactionStatus;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.IdempotencyRepository;
import com.mybank.atmweb.repository.TransactionRepository;
import com.mybank.atmweb.service.transfer.model.OperationContext;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import com.mybank.atmweb.service.transfer.model.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalDAWService {

    private final TransactionRepository txRepo;
    private final IdempotencyRepository idemRepo;
    private final AccountRepository accRepo;

    public OperationSummary internalDeposit(OperationContext ctx) {

        //멱등성
        if (idemRepo.existsByKey(ctx.getIdempotencyKey())) {
            Transactions existing = txRepo.findByIdempotencyKey(ctx.getIdempotencyKey())
                    .orElseThrow(() -> new CustomException(ErrorCode.IDEMPOTENCY_KEY_NOT_FOUND));
            return new OperationSummary(SuccessCode.TRANSFER_OK.name(), SuccessCode.TRANSFER_OK.getMessageKey(),
                    TransactionStatus.COMPLETED, existing.getId());
        }

        Account acc = accRepo.findById(ctx.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        //계좌 검증
        validateAccountUsable(acc);
        validateOwnership(acc, ctx.getUserId());

        //입금 적용
        long before = acc.getBalance();
        acc.deposit(ctx.getAmount());

        //트랜잭션 적용
        Transactions tx = txRepo.save(
                Transactions.builder()
                        .account(acc)
                        .operationType(OperationType.DEPOSIT)
                        .amount(ctx.getAmount())
                        .balanceBefore(before)
                        .balanceAfter(acc.getBalance())
                        .memo(ctx.getMemo())
                        .toAccountNumber(ctx.getToAccountNumber())
                        .toBank(ctx.getToBank())
                        .transactionStatus(TransactionStatus.COMPLETED)
                        .idempotencyKey(ctx.getIdempotencyKey())
                        .build()
        );

        idemRepo.save(new Idempotency(ctx.getIdempotencyKey(), tx.getId(), tx.getCreatedAt(), TransactionStatus.COMPLETED, null));

        return new OperationSummary(
                SuccessCode.DEPOSIT_OK.name(),
                SuccessCode.DEPOSIT_OK.getMessageKey(),
                TransactionStatus.COMPLETED,
                ctx.getUserId());
    }

    public OperationSummary internalWithdraw(OperationContext ctx) {

        //멱등성
        if (idemRepo.existsByKey(ctx.getIdempotencyKey())) {
            Transactions existing = txRepo.findByIdempotencyKey(ctx.getIdempotencyKey())
                    .orElseThrow(() -> new CustomException(ErrorCode.IDEMPOTENCY_KEY_NOT_FOUND));
            return new OperationSummary(SuccessCode.TRANSFER_OK.name(), SuccessCode.TRANSFER_OK.getMessageKey(),
                    TransactionStatus.COMPLETED, existing.getId());
        }

        Account acc = accRepo.findById(ctx.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        //계좌 검증
        validateAccountUsable(acc);
        validateOwnership(acc, ctx.getUserId());
        ensureSufficientBalance(acc, ctx.getAmount());

        //출금 적용
        long before = acc.getBalance();
        acc.withdraw(ctx.getAmount());

        //트랜잭션 적용
        Transactions tx = txRepo.save(
                Transactions.builder()
                        .account(acc)
                        .operationType(OperationType.WITHDRAW)
                        .amount(ctx.getAmount())
                        .balanceBefore(before)
                        .balanceAfter(acc.getBalance())
                        .memo(ctx.getMemo())
                        .fromAccountNumber(ctx.getFromAccountNumber())
                        .fromBank(BankType.valueOf(ctx.getFromBank()))
                        .transactionStatus(TransactionStatus.COMPLETED)
                        .idempotencyKey(ctx.getIdempotencyKey())
                        .build()
        );

        idemRepo.save(new Idempotency(ctx.getIdempotencyKey(), tx.getId(), tx.getCreatedAt(), TransactionStatus.COMPLETED, null));

        return new OperationSummary(
                SuccessCode.WITHDRAW_OK.name(),
                SuccessCode.WITHDRAW_OK.getMessageKey(),
                TransactionStatus.COMPLETED,
                ctx.getUserId());
    }

    public void validateAccountUsable(Account acc) {
        if (!acc.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
    }

    private void validateOwnership(Account acc, Long userId) {
        if (!acc.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private void ensureSufficientBalance(Account from, long amount) {
        if (from.getBalance() < amount) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }
    }
}

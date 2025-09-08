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

import java.util.List;

@Service
@RequiredArgsConstructor
public class InternalTransferService {

    private final IdempotencyRepository idemRepo;
    private final TransactionRepository txRepo;
    private final AccountRepository accountRepo;

    public OperationSummary transferInternal(OperationContext ctx) {
        // 멱등성
        if (idemRepo.existsByKey(ctx.getIdempotencyKey())) {
            Transactions existing = txRepo.findMasterByIdempotencyKeyAndOperationType(ctx.getIdempotencyKey(), ctx.getOperationType())
                    .orElseThrow(() -> new CustomException(ErrorCode.IDEMPOTENCY_KEY_NOT_FOUND));

            return new OperationSummary(SuccessCode.TRANSFER_OK.name(), SuccessCode.TRANSFER_OK.getMessageKey(),
            TransactionStatus.COMPLETED, existing.getId());
        }

        // Id 기준으로 순서 매기기
        long fromId = accountRepo.findIdByAccountNumber(ctx.getFromAccountNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        long toId = accountRepo.findIdByAccountNumber(ctx.getToAccountNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        long firstId = Math.min(fromId, toId);
        long secondId = Math.max(fromId, toId);

        // 락
        Account first = accountRepo.findById(firstId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        Account second = accountRepo.findById(secondId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        // 의미 매핑
        Account from = first.getAccountNumber().equals(ctx.getFromAccountNumber()) ? first : second;
        Account to = from == first ? second : first;

        // 검증 1 - 로직
        validateSameBankMybank(from, to);

        // 검증 2 - 유효한 계좌 상태
        validateAccountUsable(from, to);

        // 검증 3 - 이체 송금인
        validateOwnership(from, ctx.getUserId());

        // 검증 4 - 충분한 잔액
        ensureSufficientBalance(from, ctx.getAmount());

        long fromBefore = from.getBalance();
        long toBefore = from.getBalance();

        from.withdraw(ctx.getAmount());
        to.deposit(ctx.getAmount());

        Transactions master = txRepo.save(
                Transactions.builder()
                        .master(true)
                        .operationType(OperationType.TRANSFER)
                        .fromAccountNumber(ctx.getFromAccountNumber())
                        .toAccountNumber(ctx.getToAccountNumber())
                        .amount(ctx.getAmount())
                        .memo(ctx.getMemo())
                        .transactionStatus(TransactionStatus.PENDING_WITHDRAW)
                        .idempotencyKey(ctx.getIdempotencyKey())
                        .build()
        );

        txRepo.saveAll(List.of(
                Transactions.builder()
                        .parent(master)
                        .account(from)
                        .operationType(OperationType.WITHDRAW)
                        .amount(ctx.getAmount())
                        .balanceBefore(fromBefore)
                        .balanceAfter(from.getBalance())
                        .memo(ctx.getMemo())
                        .fromBank(BankType.valueOf(ctx.getFromBank()))
                        .toBank(ctx.getToBank())
                        .fromAccountNumber(ctx.getFromAccountNumber())
                        .toAccountNumber(ctx.getToAccountNumber())
                        .transactionStatus(TransactionStatus.COMPLETED)
                        .build(),
                Transactions.builder()
                        .parent(master)
                        .account(to)
                        .operationType(OperationType.DEPOSIT)
                        .amount(ctx.getAmount())
                        .balanceBefore(toBefore)
                        .balanceAfter(to.getBalance())
                        .memo(ctx.getMemo())
                        .fromBank(BankType.valueOf(ctx.getFromBank()))
                        .toBank(ctx.getToBank())
                        .fromAccountNumber(ctx.getFromAccountNumber())
                        .toAccountNumber(ctx.getToAccountNumber())
                        .transactionStatus(TransactionStatus.COMPLETED)
                        .build()
        ));

        // 마스터 확정
        master.setTransactionStatus(TransactionStatus.COMPLETED);
        txRepo.save(master);
        idemRepo.save(new Idempotency(master.getIdempotencyKey(), master.getId(), master.getCreatedAt(), TransactionStatus.COMPLETED, null));

        return new OperationSummary(
                SuccessCode.TRANSFER_OK.name(),
                SuccessCode.TRANSFER_OK.getMessageKey(),
                TransactionStatus.COMPLETED,
                ctx.getUserId());
    }

    private void validateSameBankMybank(Account from, Account to) {
        if (!from.getBank().equals(BankType.MYBANK) || !to.getBank().equals(BankType.MYBANK)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_TRANSFER);
        }
    }

    private void validateAccountUsable(Account from, Account to) {
        if (!from.getStatus().equals(AccountStatus.ACTIVE) || !to.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
    }

    private void validateOwnership(Account from, Long userId) {
        if (!from.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private void ensureSufficientBalance(Account from, long amount) {
        if (from.getBalance() < amount) {
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }
    }
}

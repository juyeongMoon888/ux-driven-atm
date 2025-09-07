package com.mybank.atmweb.application.command;

import com.mybank.atmweb.application.query.AccountQueryService;
import com.mybank.atmweb.application.query.TransactionQueryService;
import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.domain.Idempotency;
import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.domain.transaction.Transactions;
import com.mybank.atmweb.dto.ExAccDepositRes;
import com.mybank.atmweb.dto.MemoUpdateRequest;
import com.mybank.atmweb.dto.TransactionStatus;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.IdempotencyRepository;
import com.mybank.atmweb.repository.TransactionRepository;
import com.mybank.atmweb.service.IdempotencyService;
import com.mybank.atmweb.service.transfer.model.OperationContext;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import com.mybank.atmweb.service.transfer.model.OperationType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionCommandService {
    private final TransactionQueryService transactionQueryService;
    private final TransactionRepository transactionRepository;
    private final AccountQueryService accountQueryService;
    private final TransactionRepository txRepo;
    private final IdempotencyRepository idemRepo;

    @Transactional
    public String updateTransactionMemo(Long transactionId, Long userId, MemoUpdateRequest memoRequest) {
        String memo = memoRequest.getMemo();
        Transactions tx = transactionQueryService.getTransactionOrThrow(transactionId, userId);
        tx.setMemo(memo);
        return tx.getAccount().getAccountNumber();
    }

    @Transactional
    public void save(Transactions tx) {
        transactionRepository.save(tx);
    }

    @Transactional
    public Long createPendingWithdraw(OperationContext ctx) {
        // 멱등 마스터 선삽입
        Transactions newMaster = Transactions.builder()
                .master(true)
                .operationType(OperationType.TRANSFER)
                .fromAccountNumber(ctx.getFromAccountNumber())
                .toAccountNumber(ctx.getToAccountNumber())
                .amount(ctx.getAmount())
                .memo(ctx.getMemo())
                .transactionStatus(TransactionStatus.PENDING_WITHDRAW)
                .idempotencyKey(ctx.getIdempotencyKey())
                .build();
        txRepo.save(newMaster);
        txRepo.flush();

        // 스냅샷
        Account account = accountQueryService.findAccountByAccountNumberAndUserId(ctx.getFromAccountNumber(), ctx.getUserId());
        long before = account.getBalance();
        account.withdraw(ctx.getAmount());

        // leg 생성/저장 (PENDING)
        Transactions withdrawLeg = Transactions.builder()
                .parent(newMaster)
                .account(account)
                .operationType(OperationType.WITHDRAW)
                .amount(ctx.getAmount())
                .balanceBefore(before)
                .balanceAfter(account.getBalance())
                .memo(ctx.getMemo())
                .fromBank(BankType.valueOf(ctx.getFromBank()))
                .toBank(ctx.getToBank())
                .fromAccountNumber(ctx.getFromAccountNumber())
                .toAccountNumber(ctx.getToAccountNumber())
                .transactionStatus(TransactionStatus.PENDING) // ❓COMPLETE로 해야하나
                .build();
        txRepo.save(withdrawLeg);

        // master 상태 전이: PENDING_EXTERNAL (외부 입금 대기)
        newMaster.setTransactionStatus(TransactionStatus.PENDING_EXTERNAL);
        txRepo.save(newMaster);

        return newMaster.getId();
    }

    @Transactional
    public Long createDepositApplied(OperationContext ctx) {
        //멱등성 마스터 선삽입
        Transactions newMaster = Transactions.builder()
                .master(true)
                .operationType(OperationType.TRANSFER)
                .fromBank(BankType.valueOf(ctx.getFromBank()))
                .fromAccountNumber(ctx.getFromAccountNumber())
                .toBank(ctx.getToBank())
                .toAccountNumber(ctx.getToAccountNumber())
                .amount(ctx.getAmount())
                .memo(ctx.getMemo())
                .transactionStatus(TransactionStatus.PENDING_INTERNAL)
                .idempotencyKey(ctx.getIdempotencyKey())
                .build();
        txRepo.save(newMaster);
        txRepo.flush();

        //스냅샷
        Account to = accountQueryService
                .findAccountByAccountNumberAndUserId(ctx.getToAccountNumber(), ctx.getUserId());
        long before = to.getBalance();
        //실제 입금
        to.deposit(ctx.getAmount());

        //leg 생성/저장 COMPLETE
        Transactions depositLeg = Transactions.builder()
                .parent(newMaster)
                .account(to)
                .operationType(OperationType.DEPOSIT)
                .amount(ctx.getAmount())
                .balanceBefore(before)
                .balanceAfter(to.getBalance())
                .memo(ctx.getMemo())
                .fromBank(BankType.valueOf(ctx.getFromBank()))
                .toBank(ctx.getToBank())
                .fromAccountNumber(ctx.getFromAccountNumber())
                .toAccountNumber(ctx.getToAccountNumber())
                .transactionStatus(TransactionStatus.COMPLETED)
                .build();
        txRepo.save(depositLeg);

        // master 상태 전이: PENDING_CONFIRM (외부 승인 대기)
        newMaster.setTransactionStatus(TransactionStatus.PENDING_CONFIRM);
        txRepo.save(newMaster);

        return newMaster.getId();
    }

    @Transactional
    public void markOutboundFailed(Long txId) {
        Transactions tx = txRepo.findByIdForUpdate(txId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (tx.getTransactionStatus() != TransactionStatus.COMPLETED) {
            tx.setTransactionStatus(TransactionStatus.FAILED);
            txRepo.save(tx);
        }
    }

    public void markOutboundCompleted(Long txId) {
        Transactions tx = txRepo.findByIdForUpdate(txId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        tx.setTransactionStatus(TransactionStatus.COMPLETED);
        txRepo.save(tx);
    }

    @Transactional
    public void markInboundFailed(OperationContext ctx, String errorCode) {
        // 1) 마스터 INSERT-FIRST (여기서 유니크 충돌 시 예외 발생 → 호출부에서 summarizeExisting)
        Transactions newMaster = Transactions.builder()
                .master(true)
                .operationType(OperationType.TRANSFER)
                .fromBank(BankType.valueOf(ctx.getFromBank()))
                .toBank(ctx.getToBank())
                .fromAccountNumber(ctx.getFromAccountNumber())
                .toAccountNumber(ctx.getToAccountNumber())
                .amount(ctx.getAmount())
                .memo(ctx.getMemo())
                .transactionStatus(TransactionStatus.FAILED)
                .idempotencyKey(ctx.getIdempotencyKey())
                .failureCode(errorCode)
                .build();
        // 2) 멱등키 종결 기록(중복 무시)
        txRepo.save(newMaster);
        idemRepo.flush();

        idemRepo.save(new Idempotency(
                ctx.getIdempotencyKey(),
                newMaster.getId(),
                LocalDateTime.now(),
                TransactionStatus.FAILED,
                errorCode));
    }

    @Transactional
    public void markAwaitingExternalConfirm(Long txId, String reasonCode) {
        Transactions m = txRepo.findMasterByIdForUpdate(txId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));
        m.setTransactionStatus(TransactionStatus.PENDING_CONFIRM);
        m.setFailureCode(reasonCode);
        txRepo.save(m);
    }

    @Transactional
    public void markInboundConfirmedMaster(Long txId) {
        Transactions tx = txRepo.findMasterByIdForUpdate(txId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        tx.setTransactionStatus(TransactionStatus.COMPLETED);
        txRepo.save(tx);
    }

    @Transactional
    public void markRelayFailed(OperationContext ctx, String reasonCode) {
        Transactions m = Transactions.builder()
                .master(true)
                .operationType(OperationType.TRANSFER)
                .fromBank(BankType.valueOf(ctx.getFromBank()))
                .fromAccountNumber(ctx.getFromAccountNumber())
                .toBank(ctx.getToBank())
                .toAccountNumber(ctx.getToAccountNumber())
                .amount(ctx.getAmount())
                .transactionStatus(TransactionStatus.FAILED)
                .failureCode(reasonCode)
                .idempotencyKey(ctx.getIdempotencyKey())
                .build();
        txRepo.save(m);
        txRepo.flush();

        // 멱등키 종결
        idemRepo.save(new Idempotency(
                ctx.getIdempotencyKey(),
                m.getId(),
                LocalDateTime.now(),
                TransactionStatus.FAILED,
                reasonCode));
    }

    public void markRelayPendingConfirm(OperationContext ctx, Long exTxId) {
    }

    public void markRelayCompleted(OperationContext ctx, Long exTxId) {
    }
}


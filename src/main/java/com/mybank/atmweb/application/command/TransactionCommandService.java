package com.mybank.atmweb.application.command;

import com.mybank.atmweb.application.query.AccountQueryService;
import com.mybank.atmweb.application.query.TransactionQueryService;
import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.domain.transaction.Transactions;
import com.mybank.atmweb.dto.ExAccDepositRes;
import com.mybank.atmweb.dto.MemoUpdateRequest;
import com.mybank.atmweb.dto.TransactionStatus;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.TransactionRepository;
import com.mybank.atmweb.service.transfer.model.OperationContext;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import com.mybank.atmweb.service.transfer.model.OperationType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionCommandService {
    private final TransactionQueryService transactionQueryService;
    private final TransactionRepository transactionRepository;
    private final AccountQueryService accountQueryService;
    private final TransactionRepository txRepo;

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

    /*@Transactional
    public Long createPendingDeposit(OperationContext ctx) {
        //1) 입력 정규화
        BankType fromBank = BankType.valueOf(ctx.getFromBank());

        //2) 계좌 조회 및 입금
        Account account = accountQueryService.findAccountByAccountNumberAndUserId(ctx.getFromAccountNumber(), ctx.getUserId());
        long before = account.getBalance();
        account.deposit(ctx.getAmount());
        long after  = account.getBalance();

        //3) 트랜잭션(PENDING) 생성/저장
        Transactions tx = Transactions.builder()
                .account(account)
                .operationType(OperationType.DEPOSIT)
                .amount(ctx.getAmount())
                .balanceBefore(before)
                .balanceAfter(after)
                .memo(ctx.getMemo())
                .fromBank(fromBank)
                .toBank(ctx.getToBank())                // enum/코드 타입 일관성 유지
                .fromAccountNumber(ctx.getFromAccountNumber())
                .toAccountNumber(ctx.getToAccountNumber())
                .transactionStatus(TransactionStatus.PENDING)
                .build();

        return transactionRepository.save(tx).getId();
    }

    @Transactional
    public Long createPendingInboundDeposit(OperationContext ctx) {
        // 🔎 목적지 기준: toAccount가 MYBANK(내부) 계좌
        Account to = accountQueryService
                .findAccountByAccountNumberAndUserId(ctx.getToAccountNumber(), ctx.getUserId());

        Transactions tx = Transactions.builder()
                .account(to)
                .operationType(OperationType.DEPOSIT)
                .amount(ctx.getAmount())
                .balanceBefore(to.getBalance())     // 참고용 스냅샷
                .balanceAfter(to.getBalance())      // 아직 변경하지 않음
                .memo(ctx.getMemo())
                .fromBank(BankType.valueOf(ctx.getFromBank()))
                .toBank(ctx.getToBank())
                .fromAccountNumber(ctx.getFromAccountNumber())
                .toAccountNumber(ctx.getToAccountNumber())
                .transactionStatus(TransactionStatus.PENDING)
                .build();

        return transactionRepository.save(tx).getId();
    }

    @Transactional
    public OperationSummary completeInboundDeposit(Long txId, OperationContext ctx) {
        Transactions tx = transactionRepository.findByIdForUpdate(txId)  // 낙관/비관 잠금 중 택1
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (tx.getTransactionStatus() != TransactionStatus.PENDING) {
            return new OperationSummary(SuccessCode.ALREADY_FINALIZED.name(), SuccessCode.ALREADY_FINALIZED.getMessageKey(), TransactionStatus.FAILED, ctx.getUserId());
        }

        Account to = tx.getAccount(); // createPending에서 이미 to 계좌로 연결
        long before = to.getBalance();
        to.deposit(ctx.getAmount());
        long after = to.getBalance();

        tx.setBalanceBefore(before);
        tx.setBalanceAfter(after);
        tx.setTransactionStatus(TransactionStatus.COMPLETED);

        return new OperationSummary(SuccessCode.DEPOSIT_OK.name(), SuccessCode.DEPOSIT_OK.getMessageKey(), TransactionStatus.COMPLETED, ctx.getUserId());
    }

    @Transactional
    public void markOutboundFailed(Long txId, ExAccDepositRes dres) {
        Transactions tx = txRepo.findByIdForUpdate(txId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (tx.getTransactionStatus() != TransactionStatus.COMPLETED) {
            tx.setTransactionStatus(TransactionStatus.FAILED);
            txRepo.save(tx);
        }
    }*/

    public void markOutboundCompleted(Long txId) {
        Transactions tx = txRepo.findByIdForUpdate(txId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        tx.setTransactionStatus(TransactionStatus.COMPLETED);
        txRepo.save(tx);
    }

    public void markInboundFailed(OperationContext ctx, String code) {

    }

    public void markInboundPendingConfirm(Long txId) {

    }

    public void markInboundConfirmed(Long txId) {
        Transactions tx = txRepo.findByIdForUpdate(txId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        tx.setTransactionStatus(TransactionStatus.COMPLETED);
        txRepo.save(tx);
    }

    public void markFailedBusiness(OperationContext ctx, String code) {
        //외부 -> 외부인데.. 외부1차 출금때부터 실패한거임
    }

    public void markRelayPendingConfirm(OperationContext ctx, Long exTxId) {
    }

    public void markRelayCompleted(OperationContext ctx, Long exTxId) {
    }
}


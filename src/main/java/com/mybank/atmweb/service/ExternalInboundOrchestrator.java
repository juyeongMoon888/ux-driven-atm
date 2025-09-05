package com.mybank.atmweb.service;

import com.mybank.atmweb.application.command.TransactionCommandService;
import com.mybank.atmweb.application.query.AccountQueryService;
import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.domain.transaction.Transactions;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.TransactionRepository;
import com.mybank.atmweb.service.transfer.model.OperationContext;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import com.mybank.atmweb.service.transfer.model.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalInboundOrchestrator {

    private final ExternalBankClient externalBankClient;
    private final TransactionCommandService txCmd;
    private final AccountQueryService accountQueryService;
    private final TransactionRepository txRepo;

    /**
     *
     * markInboundConfirmedMaster(txId)는 마스터 상태만 COMPLETED로 전이(레그/잔액은 건드리지 않음).
     *
     * 멱등은 마스터 UNIQ(idempotencyKey, operationType, master=true)와 예외 캐치 → summarizeExisting로 처리.
     *
     * 만약 외부 confirm이 실패하면 PENDING_CONFIRM로 두고 재시도/보정 정책을 운영(필요 시 리버설 레그).
     * 원자성: 잔액 증가와 레그 생성이 항상 함께 일어나야 무결성 보장.
     *
     * 경합/재시도 내성: 마스터/레그/잔액을 한 트랜잭션에 묶으면 멱등 처리 단순화.
     *
     * 확실한 책임 분리: 내부 서버가 입금까지 확정하고, 외부 confirm은 마스터 상태 종결만.
     *
     */
    public OperationSummary inboundToMybank(OperationContext ctx) {
        ExAccWithdrawRes wres = externalBankClient.withdraw(ExAccWithdrawReq.fromTransfer(ctx));
        // 0) 실패 기록
        if (!wres.isApproved()) {
            try {
                txCmd.markInboundFailed(ctx, wres.getCode());
            } catch (DataIntegrityViolationException dup) {
                return summarizeExisting(ctx);
            }

            return new OperationSummary(wres.getCode(), wres.getMessage(), TransactionStatus.FAILED, null);
        }

        Long exTxId = wres.getExTxId(); // 외부 출금 성공 후 외부에 생성된 txId

        // 2) 우리 내부 입금
        Long txId;
        try {
            txId = txCmd.createDepositApplied(ctx);
        } catch (DataIntegrityViolationException dup) {
            return summarizeExisting(ctx);
        }

        // 3) 외부 confirm - 외부 confirm은 출금 레그 확정만, 마스터 전이는 MyBank 쪽에서 하세요.
        ExAccConfirmRes cres = externalBankClient.confirm(new ExAccConfirmReq(ctx.getFromBank(), exTxId));
        if (!cres.isComplete()) {
            txCmd.markInboundPendingConfirm(txId); //실패
            return new OperationSummary("PENDING_CONFIRM", "external.confirm.pending", TransactionStatus.PENDING_CONFIRM, txId);
        }

        // 4) 우리 내부 confirm  마스터, leg 반영
        txCmd.markInboundConfirmed(txId, after, ctx); //markInboundPendingConfirm

        return new OperationSummary(
                SuccessCode.TRANSFER_OK.name(),
                SuccessCode.TRANSFER_OK.getMessageKey(),
                TransactionStatus.COMPLETED,
                ctx.getUserId());
    }

    private OperationSummary summarizeExisting(OperationContext ctx) {
        Transactions existing = txRepo.findMasterByIdempotencyKeyAndOperationType(ctx.getIdempotencyKey(), OperationType.TRANSFER)
                .orElseThrow(() -> new CustomException(ErrorCode.IDEMPOTENCY_KEY_NOT_FOUND));

        switch (existing.getTransactionStatus()) {
            case COMPLETED:
                return new OperationSummary(
                        SuccessCode.TRANSFER_OK.name(),
                        SuccessCode.TRANSFER_OK.getMessageKey(),
                        TransactionStatus.COMPLETED,
                        existing.getId()
                );
            case FAILED:
                return new OperationSummary(
                        "FAILURE_CODE",
                        "FAILURE_MESSAGE",
                        TransactionStatus.FAILED,
                        existing.getId()
                );
            case PENDING_EXTERNAL:
            case PENDING_WITHDRAW:
            default:
                return new OperationSummary("PENDING",
                        "transfer.pending",
                        TransactionStatus.PENDING_EXTERNAL,
                        existing.getId());
        }
    }
}

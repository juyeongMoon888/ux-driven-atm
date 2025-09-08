package com.mybank.atmweb.service;

import com.mybank.atmweb.application.command.TransactionCommandService;
import com.mybank.atmweb.domain.transaction.Transactions;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.PendingCode;
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
public class ExternalToExternalOrchestrator {

    private final ExternalBankClient externalBankClient;
    private final TransactionCommandService txCmd;
    private final TransactionRepository txRepo;

    public OperationSummary relay(OperationContext ctx) {
        // 0) 마스터 선삽입
        Long txId; //마스터 ID
        try {
            txId = txCmd.createRelayMaster(ctx);
        } catch (DataIntegrityViolationException dup) {
            return summarizeExisting(ctx);
        }

        // 1) 출금 은행 A 승인
        ExAccWithdrawReq wreq = ExAccWithdrawReq.fromTransfer(ctx);
        ExAccWithdrawRes wres = externalBankClient.withdraw(wreq);
        if (!wres.isApproved()) {
            try {
                txCmd.markRelayFailed(ctx, wres.getCode());
            } catch (DataIntegrityViolationException dup) {
                return summarizeExisting(ctx);
            }
            return new OperationSummary(
                    wres.getCode(),
                    wres.getMessage(),
                    TransactionStatus.FAILED,
                    null);
        }

        Long exWithdrawTxId = wres.getExTxId();

        // 2) 입금 은행 B 처리
        ExAccDepositReq dreq = ExAccDepositReq.fromTransfer(ctx, exWithdrawTxId);
        ExAccDepositRes dres = externalBankClient.deposit(dreq);
        if (!dres.isSuccess()) {
            //외부 서버에서 환불 로직
            externalBankClient.cancel(new ExAccCancelReq(ctx.getFromBank(), exWithdrawTxId));
            txCmd.markRelayFailed(ctx, dres.getCode());
            return new OperationSummary(
                    dres.getCode(),
                    dres.getMessage(),
                    TransactionStatus.FAILED,
                    null);
        }

        Long exDepositTxId = dres.getExTxId();

        // 3) 출금 은행 A 최종 확정
        try {
            externalBankClient.confirm(new ExAccConfirmReq(ctx.getFromBank(), exWithdrawTxId));
        } catch (Exception ex) {
            txCmd.markAwaitingExternalConfirm(txId, "CONFIRM_UNREACHABLE:" + ctx.getFromBank());
            return new OperationSummary(
                    PendingCode.PENDING_CONFIRM.name(),
                    PendingCode.PENDING_CONFIRM.getMessageKey(),
                    TransactionStatus.PENDING_CONFIRM, txId
            );
        }

        // 4. 입금 은행 B 최종 확정
        try {
            externalBankClient.confirm(new ExAccConfirmReq(ctx.getToBank(), exDepositTxId));
        } catch (Exception ex) {
            txCmd.markAwaitingExternalConfirm(txId, "CONFIRM_UNREACHABLE:" + ctx.getToBank());
            return new OperationSummary(
                    PendingCode.PENDING_CONFIRM.name(),
                    PendingCode.PENDING_CONFIRM.getMessageKey(),
                    TransactionStatus.PENDING_CONFIRM, txId
            );
        }

        txCmd.markRelayCompleted(ctx, txId); //최종 complete
        return new OperationSummary("EXT_TO_EXT_OK", "외부->외부 송금 성공", TransactionStatus.COMPLETED, null);
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

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
    private final TransactionRepository txRepo;

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
            txId = txCmd.createDepositApplied(ctx); //마스터 ID
        } catch (DataIntegrityViolationException dup) {
            return summarizeExisting(ctx);
        }

        try {
            //3) 외부 confirm
            ExAccConfirmRes cres = externalBankClient.confirm(new ExAccConfirmReq(ctx.getFromBank(), exTxId));
            //4) 내부 confirm - 마스터 complete
            txCmd.markInboundConfirmedMaster(txId);
        } catch (Exception ex) { //도메인 오류
            txCmd.markAwaitingExternalConfirm(txId, "CONFIRM_UNREACHABLE");
            return new OperationSummary("PENDING_CONFIRM",
                    "external.confirm.pending",
                    TransactionStatus.PENDING_CONFIRM,
                    txId);
        }

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

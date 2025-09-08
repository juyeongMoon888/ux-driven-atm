package com.mybank.atmweb.service;

import com.mybank.atmweb.application.command.TransactionCommandService;
import com.mybank.atmweb.domain.transaction.Transactions;
import com.mybank.atmweb.dto.ExAccDepositReq;
import com.mybank.atmweb.dto.ExAccDepositRes;
import com.mybank.atmweb.dto.TransactionStatus;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.IdempotencyRepository;
import com.mybank.atmweb.repository.TransactionRepository;
import com.mybank.atmweb.service.transfer.model.OperationContext;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import com.mybank.atmweb.service.transfer.model.OperationType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboundToExternalOrchestrator {

    private final TransactionCommandService txCmd;
    private final AccountService accountService;
    private final ExternalBankClient externalBankClient;
    private final TransactionRepository txRepo;

    @Transactional
    public OperationSummary outbound(OperationContext ctx) {
        // 1. 내부 계좌 출금 & PENDING 기록
        Long txId;
        try {
            txId = txCmd.createPendingWithdraw(ctx);
        } catch (DataIntegrityViolationException dup) {
            return summarizeExisting(ctx);
        }

        // 2. 외부 은행 입금 호출
        ExAccDepositReq dreq = ExAccDepositReq.fromTransfer(ctx);
        ExAccDepositRes dres;
        try {
            dres = externalBankClient.deposit(dreq);
        } catch (Exception ex) {
            //예외 보상
            accountService.refundOutboundIfNeeded(ctx.getIdempotencyKey(), ctx.getFromAccountNumber(), ctx.getAmount(), "외부 입금 호출 중 예외");
            ExAccDepositRes fakeRes = ExAccDepositRes.fail("EXTERNAL_ERROR", "외부 입금 호출 중 예외");
            txCmd.markOutboundFailed(txId);
            return new OperationSummary(fakeRes.getCode(), fakeRes.getMessage(), TransactionStatus.FAILED, txId);
        }
        if (!dres.isSuccess()) {
            txCmd.markOutboundFailed(txId);
            return new OperationSummary(dres.getCode(), dres.getMessage(), TransactionStatus.FAILED, txId);
        }

        txCmd.markOutboundCompleted(txId);
        return new OperationSummary(
                SuccessCode.TRANSFER_OK.name(),
                SuccessCode.TRANSFER_OK.getMessageKey(),
                TransactionStatus.COMPLETED,
                txId);
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

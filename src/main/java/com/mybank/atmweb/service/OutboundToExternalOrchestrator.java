package com.mybank.atmweb.service;

import com.mybank.atmweb.application.command.TransactionCommandService;
import com.mybank.atmweb.dto.ExAccDepositReq;
import com.mybank.atmweb.dto.ExAccDepositRes;
import com.mybank.atmweb.dto.TransactionStatus;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.service.transfer.model.OperationContext;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboundToExternalOrchestrator {

    private final TransactionCommandService txCmd;
    private final AccountService accountService;
    private final ExternalBankClient externalBankClient;

    @Transactional
    public OperationSummary outbound(OperationContext ctx) {
        // 1. 내부 계좌 출금 & PENDING 기록
        Long txId = txCmd.createPendingWithdraw(ctx);
        accountService.withdraw(ctx.getFromAccountNumber(), ctx.getAmount(), ctx.getMemo(), ctx.getUserId(), ctx.getIdempotencyKey());

        // 2. 외부 은행 입금 호출
        ExAccDepositReq dreq = ExAccDepositReq.from(ctx, txId);
        ExAccDepositRes dres;
        try {
            dres = externalBankClient.deposit(dreq);
        } catch (Exception ex) {
            //예외 보상
            accountService.refundOutboundIfNeeded(ctx.getIdempotencyKey(), ctx.getFromAccountNumber(), ctx.getAmount(), "외부 입금 호출 중 예외");
            ExAccDepositRes fakeRes = ExAccDepositRes.fail("EXTERNAL_ERROR", "외부 입금 호출 중 예외");
            txCmd.markOutboundFailed(txId, fakeRes);

            return new OperationSummary(fakeRes.getCode(), fakeRes.getMessage(), TransactionStatus.FAILED, txId);
        }
        if (!dres.isSuccess()) {
            txCmd.markOutboundFailed(txId, dres);
            return new OperationSummary(dres.getCode(), dres.getMessage(), TransactionStatus.FAILED, txId);
        }

        txCmd.markOutboundCompleted(txId);
        return new OperationSummary(
                SuccessCode.TRANSFER_OK.name(),
                SuccessCode.TRANSFER_OK.getMessageKey(),
                TransactionStatus.COMPLETED,
                txId);

    }
}

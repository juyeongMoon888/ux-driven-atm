package com.mybank.atmweb.service;

import com.mybank.atmweb.application.command.TransactionCommandService;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.service.transfer.model.OperationContext;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalToExternalOrchestrator {

    private final ExternalBankClient externalBankClient;
    private final TransactionCommandService txCmd;

    public OperationSummary relay(OperationContext ctx) {
        //1. 출금 은행 A 승인
        ExAccWithdrawReq wreq = ExAccWithdrawReq.fromTransfer(ctx);
        ExAccWithdrawRes wres = externalBankClient.withdraw(wreq);

        if (!wres.isApproved()) {
            txCmd.markFailedBusiness(ctx, wres.getCode());
            return new OperationSummary(
                    wres.getCode(),
                    wres.getMessage(),
                    TransactionStatus.FAILED,
                    null
            );
        }

        Long exTxId = wres.getExTxId();

        // 2. 입금 은행 B 처리
        ExAccDepositReq dreq = ExAccDepositReq.fromTransfer(ctx, exTxId);
        ExAccDepositRes dres = externalBankClient.deposit(dreq);

        if (!dres.isSuccess()) {
            externalBankClient.cancel(new ExAccCancelReq(ctx.getFromBank(), exTxId));
            txCmd.markFailedBusiness(ctx, dres.getCode());
            return new OperationSummary(
                    dres.getCode(),
                    dres.getMessage(),
                    TransactionStatus.FAILED,
                    null);
        }

        // 3. 출금 은행 A 최종 확정
        ExAccConfirmRes cres = externalBankClient.confirm(new ExAccConfirmReq(ctx.getFromBank(), exTxId));
        if (!cres.isComplete()) {
            txCmd.markRelayPendingConfirm(ctx, exTxId);
            return new OperationSummary(
                    "PENDING_CONFIRM",
                    "external.confirm.pending",
                    TransactionStatus.PENDING,
                    null);
        }

        // 4. 입금 은행 B 최종 확정
        ExAccConfirmRes cresB = externalBankClient.confirm(new ExAccConfirmReq(ctx.getToBank(), exTxId));
        if (!cresB.isComplete()) {
            txCmd.markRelayPendingConfirm(ctx, exTxId);
            return new OperationSummary(
                    "PENDING_CONFIRM_B",
                    "external.confirm.pending.B",
                    TransactionStatus.PENDING,
                    null);
        }

        txCmd.markRelayCompleted(ctx, exTxId);
        return new OperationSummary("EXT_TO_EXT_OK", "외부->외부 송금 성공", TransactionStatus.COMPLETED, null);
    }
}

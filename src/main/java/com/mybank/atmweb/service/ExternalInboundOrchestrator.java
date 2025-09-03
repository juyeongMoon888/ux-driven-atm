package com.mybank.atmweb.service;

import com.mybank.atmweb.application.command.TransactionCommandService;
import com.mybank.atmweb.application.query.AccountQueryService;
import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.domain.account.AccountRepository;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.repository.IdempotencyRepository;
import com.mybank.atmweb.service.transfer.model.OperationContext;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalInboundOrchestrator {

    private final ExternalBankClient externalBankClient;
    private final TransactionCommandService txCmd;
    private final AccountQueryService accountQueryService;

    public OperationSummary inboundToMybank(OperationContext ctx) {
        ExAccWithdrawRes wres = externalBankClient.withdraw(ExAccWithdrawReq.fromTransfer(ctx));

        if (!wres.isApproved()) {
            //외부쪽에 출금 승인이 안된것
            txCmd.markInboundFailed(ctx, wres.getCode());
            return new OperationSummary(wres.getCode(), wres.getMessage(), TransactionStatus.FAILED, null);
        }

        Long exTxId = wres.getExTxId(); //외부로 부터

        // 2) 우리 내부 입금
        Long txId = txCmd.createPendingInboundDeposit(ctx);
        Account to = accountQueryService.findAccountByAccountNumberAndUserId(ctx.getToAccountNumber(), ctx.getUserId());

        long before = to.getBalance();
        to.deposit(ctx.getAmount());

        // 3) 외부 confirm
        ExAccConfirmRes cres = externalBankClient.confirm(new ExAccConfirmReq(exTxId));
        if (!cres.isComplete()) {
            txCmd.markInboundPendingConfirm(txId);
            return new OperationSummary("PENDING_CONFIRM", "external.confirm.pending", TransactionStatus.PENDING, txId);
        }

        // 4) 우리 내부 confirm
        txCmd.markInboundConfirmed(txId);

        return new OperationSummary(
                SuccessCode.TRANSFER_OK.name(),
                SuccessCode.TRANSFER_OK.getMessageKey(),
                TransactionStatus.COMPLETED,
                ctx.getUserId());
    }
}

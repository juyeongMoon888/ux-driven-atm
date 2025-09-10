package com.mybank.atmweb.service;

import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.service.transfer.model.OperationContext;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

@Service
@RequiredArgsConstructor
public class ExternalDAWService {

    private final ExternalBankClient externalBankClient;

    public OperationSummary externalDeposit(OperationContext ctx) {
        ExAccDepositReq dreq = ExAccDepositReq.fromDeposit(ctx);

        ExAccDepositRes dres;
        try {
            dres = externalBankClient.deposit(dreq);
        } catch (HttpStatusCodeException ex) {
            return new OperationSummary(
                    "UPSTREAM_ERROR",
                    "external.deposit.upstream_error",
                    TransactionStatus.FAILED,
                    null
            );
        } catch (Exception ex) {
            return new OperationSummary(
                    "UPSTREAM_UNREACHABLE",
                    "external.deposit.unreachable",
                    TransactionStatus.FAILED,
                    null
            );
        }

        if (!dres.isSuccess()) {
            return new OperationSummary(
              dres.getCode(),
              dres.getMessage(),
              TransactionStatus.FAILED,
              null
            );
        }
        return new OperationSummary(
                dres.getCode(),
                dres.getMessage(),
                TransactionStatus.COMPLETED,
                null);
    }

    public OperationSummary externalWithdraw(OperationContext ctx) {
        ExAccWithdrawReq wreq = ExAccWithdrawReq.fromWithdraw(ctx);
        ExAccWithdrawRes wres = externalBankClient.withdraw(wreq);
        return new OperationSummary(wres.getCode(), wres.getMessage(), null, null);
    }
}

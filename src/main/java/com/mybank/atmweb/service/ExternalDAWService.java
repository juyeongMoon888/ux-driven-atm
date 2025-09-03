package com.mybank.atmweb.service;

import com.mybank.atmweb.dto.ExAccDepositReq;
import com.mybank.atmweb.dto.ExAccDepositRes;
import com.mybank.atmweb.dto.ExAccWithdrawReq;
import com.mybank.atmweb.dto.ExAccWithdrawRes;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.service.transfer.model.OperationContext;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalDAWService {

    private final ExternalBankClient externalBankClient;

    public OperationSummary externalDeposit(OperationContext ctx) {
        ExAccDepositReq dreq = ExAccDepositReq.fromDeposit(ctx);
        ExAccDepositRes dres = externalBankClient.deposit(dreq);
        return new OperationSummary(dres.getCode(), dres.getMessage(), null, null);
    }

    public OperationSummary externalWithdraw(OperationContext ctx) {
        ExAccWithdrawReq wreq = ExAccWithdrawReq.fromWithdraw(ctx);
        ExAccWithdrawRes wres = externalBankClient.withdraw(wreq);
        return new OperationSummary(wres.getCode(), wres.getMessage(), null, null);
    }
}

package com.mybank.atmweb.service.transfer;

import com.mybank.atmweb.dto.OperationSummary;
import com.mybank.atmweb.service.transfer.model.OperationContext;

public interface TransferExecutor {

    boolean supports(String bank);

    OperationSummary execute(OperationContext ctx);
}

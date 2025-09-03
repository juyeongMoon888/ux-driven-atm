package com.mybank.atmweb.service.transfer.model;

import com.mybank.atmweb.dto.TransactionStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OperationSummary {
    private final String code;
    private final String message;
    private final TransactionStatus transactionStatus;
    private final Long userId;
}

package com.mybank.atmweb.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ExAccOperationRes {
    private final TransactionStatus status;
    private final String fromAccountNumber;
    private final Long userId;
    private final Long txId;
}

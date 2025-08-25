package com.mybank.atmweb.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ExAccOperationReq {
    private final String fromBank;
    private final String fromAccountNumber;
    private final String toBank;
    private final String toAccountNumber;
    private final Long amount;
    private final String memo;
    private final Long userId;
}

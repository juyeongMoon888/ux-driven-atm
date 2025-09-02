package com.mybank.atmweb.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DepositRequestDto {
    private final String bank;
    private final String accountNumber;
    private final Long amount;
    private final String memo;
}

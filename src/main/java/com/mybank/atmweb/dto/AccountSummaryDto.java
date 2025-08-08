package com.mybank.atmweb.dto;

import com.mybank.atmweb.domain.BankType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AccountSummaryDto {
    private final BankType bankType;
    private final String accountNumber;
    private final Long balance;
}

package com.mybank.atmweb.dto;

import com.mybank.atmweb.domain.TransferType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TransferDto {
    private final TransferType type;
    private final String accountNumber;
    private final Long amount;
}

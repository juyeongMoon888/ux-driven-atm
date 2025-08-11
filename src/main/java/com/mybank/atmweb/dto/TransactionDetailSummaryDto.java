package com.mybank.atmweb.dto;

import com.mybank.atmweb.domain.TransferType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class TransactionDetailSummaryDto {
    private final LocalDateTime createdAt;
    private final TransferType transfer;
    private final Long amount;
    private final Long balanceAfter;
    private final String memo;
}
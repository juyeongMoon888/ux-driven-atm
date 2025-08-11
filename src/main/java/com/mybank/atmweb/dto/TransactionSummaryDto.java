package com.mybank.atmweb.dto;

import com.mybank.atmweb.domain.TransferType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class TransactionSummaryDto {
    private final Long id; //상세로 이동할 키
    private final LocalDateTime createdAt;
    private final TransferType transfer;
    private final Long amount;
}

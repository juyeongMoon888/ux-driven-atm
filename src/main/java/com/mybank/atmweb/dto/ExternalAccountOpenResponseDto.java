package com.mybank.atmweb.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class ExternalAccountOpenResponseDto {
    private final Long userId;
    private final String bankType;
    private final String accountNumber;
    private final String accountName;
    private final String externalAccountId;
}

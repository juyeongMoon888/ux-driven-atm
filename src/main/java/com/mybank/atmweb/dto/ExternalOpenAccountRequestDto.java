package com.mybank.atmweb.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class ExternalOpenAccountRequestDto {
    private final String bank;
    private final String accountName;
    private final Long userId;
    private final String name;
    private final String birth;
    private final String phone;
}

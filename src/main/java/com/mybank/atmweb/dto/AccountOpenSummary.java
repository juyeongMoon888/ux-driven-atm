package com.mybank.atmweb.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AccountOpenSummary {
    private final String code;
    private final String message;
}

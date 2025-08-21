package com.mybank.atmweb.dto.account.request;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AccountOpenRequestDto {
    private final String bank;
    private final String password;
    private final String accountName;
}

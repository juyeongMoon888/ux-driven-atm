package com.mybank.atmweb.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AccountOepnRequestDto {
    private final String bank;
    private final String password;
    private final String accountName;
}

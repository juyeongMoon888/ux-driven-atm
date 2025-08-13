package com.mybank.atmweb.dto;

import com.mybank.atmweb.domain.BankType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter
@RequiredArgsConstructor
public class ExternalAccountVerifyRequest {
    @NotBlank(message = "{validation.bank_type.required}")
    private final BankType bankCode;
    @NotBlank(message = "{validation.account_number.required}")
    private final String accountNumber;
}


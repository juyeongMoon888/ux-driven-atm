package com.mybank.atmweb.domain.factory;

import com.mybank.atmweb.domain.Account;
import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.ExternalAccountOpenResponseDto;

public class AccountFactory {
    private AccountFactory() {

    }


    public static Account fromExternalResponse(User user, ExternalAccountOpenResponseDto data) {
        return Account.builder()
                .owner(user)
                .bank(BankType.valueOf(data.getBankType()))
                .accountNumber(data.getAccountNumber())
                .accountName(data.getAccountName())
                .externalAccountId(data.getExternalAccountId())
                .balance(data.getBalance())
                .build();
    }
}

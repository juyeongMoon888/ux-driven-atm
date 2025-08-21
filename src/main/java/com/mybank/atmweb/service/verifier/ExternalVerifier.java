package com.mybank.atmweb.service.verifier;

import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.dto.AccountVerifyRequestDto;
import com.mybank.atmweb.dto.account.VerificationResult;
import com.mybank.atmweb.external.client.ExternalBankClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalVerifier implements AccountVerifier{
    private final ExternalBankClient externalBankClient;

    @Override
    public boolean supports(String bank) {
        return !BankType.valueOf(bank).isInternal();
    }

    @Override
    public VerificationResult doVerify(AccountVerifyRequestDto dto) {
        return externalBankClient.doVerifyAccount(dto);
    }
}

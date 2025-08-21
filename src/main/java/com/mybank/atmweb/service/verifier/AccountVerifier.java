package com.mybank.atmweb.service.verifier;

import com.mybank.atmweb.dto.AccountVerifyRequestDto;
import com.mybank.atmweb.dto.account.VerificationResult;

public interface AccountVerifier {
    VerificationResult doVerify(AccountVerifyRequestDto dto);

    boolean supports(String bank);
}

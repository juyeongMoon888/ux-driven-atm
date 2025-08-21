package com.mybank.atmweb.domain.verification;

import com.mybank.atmweb.dto.VerificationCode;
import lombok.Getter;

@Getter
public class VerificationResult {
    private final VerificationCode code;

    public VerificationResult(VerificationCode code) {
        this.code = code;
    }

    public static VerificationResult ok() {
        return new VerificationResult(VerificationCode.OK);
    }
    public static VerificationResult frozen() {
        return new VerificationResult(VerificationCode.ACCOUNT_FROZEN);
    }

    public static VerificationResult closed() {
        return new VerificationResult(VerificationCode.ACCOUNT_CLOSED);
    }

    public static VerificationResult notFound() {
        return new VerificationResult(VerificationCode.ACCOUNT_NOT_FOUND);
    }

    public static VerificationResult failed(String detail) {
        return new VerificationResult(VerificationCode.VERIFICATION_FAILED);
    }
}

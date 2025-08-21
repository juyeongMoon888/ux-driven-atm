package com.mybank.atmweb.service.verifier;

import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.dto.AccountVerifyRequestDto;
import com.mybank.atmweb.dto.VerificationCode;
import com.mybank.atmweb.dto.account.VerificationResult;
import com.mybank.atmweb.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class InternalVerifier implements AccountVerifier {
    @Override
    public boolean supports(String bank) {
        return BankType.valueOf(bank).isInternal();
    }

    private final AccountRepository accountRepository;

    @Override
    public VerificationResult doVerify(AccountVerifyRequestDto dto) {
        return accountRepository.findStatusByBankAndAccountNumber(dto.getBank(), dto.getAccountNumber())
                .map(status -> switch (status) {
                    case ACTIVE -> VerificationResult.ok();
                    case FROZEN -> VerificationResult.frozen();
                    case CLOSED -> VerificationResult.closed();
                })
                .orElseGet(VerificationResult::notFound);
    }
}

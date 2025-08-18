package com.mybank.atmweb.application;

import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountQueryService {
    private final AccountRepository accountRepository;

    @Transactional
    public Account findAccountByAccountNumberAndUserId(String accountNumber, Long userId) {
        return accountRepository.findByAccountNumberAndOwner_Id(accountNumber, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND_FOR_USER));
    }
}

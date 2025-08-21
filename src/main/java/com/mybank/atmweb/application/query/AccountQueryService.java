package com.mybank.atmweb.application.query;

import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.dto.AccountSummaryDto;
import com.mybank.atmweb.dto.account.AccountOptionDto;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountQueryService {
    private final AccountRepository accountRepository;

    @Transactional
    public Account findAccountByAccountNumberAndUserId(String accountNumber, Long userId) {
        return accountRepository.findByAccountNumberAndOwner_Id(accountNumber, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND_FOR_USER));
    }

    @Transactional
    public List<AccountSummaryDto> getListByOwner_Id(Long userId) {
        return accountRepository.findSummariesByOwnerId(userId);
    }

    @Transactional
    public List<AccountOptionDto> getOptionsByOwnerId(Long userId) {
        return accountRepository.findOptionsByOwnerId(userId);
    }
}

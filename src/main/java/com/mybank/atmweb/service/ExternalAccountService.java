package com.mybank.atmweb.service;

import com.mybank.atmweb.assembler.ExternalAccountRequestAssembler;
import com.mybank.atmweb.domain.Account;
import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.AccountRepository;
import com.mybank.atmweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalAccountService {
    private final ExternalBankClient externalBankClient;
    private final ExternalAccountRequestAssembler assembler;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public OperationSummary externalAccountOpen(AccountOpenRequestDto dto, Long userId) {

        ExternalOpenAccountRequestDto assemble = assembler.toExternalRequest(dto, userId);

        ApiResponse<ExternalAccountOpenResponseDto> response = externalBankClient.createAccount(assemble);

        ExternalAccountOpenResponseDto data = response.getData();

        User user = userRepository.findById(data.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Account externalAccount = Account.builder()
                .owner(user)
                .bank(BankType.valueOf(data.getBankType()))
                .accountNumber(data.getAccountNumber())
                .accountName(data.getAccountName())
                .externalAccountId(data.getExternalAccountId())
                .build();
        accountRepository.save(externalAccount);

        return new OperationSummary(response.getCode(), response.getMessage());
    }
}

package com.mybank.atmweb.service;

import com.mybank.atmweb.assembler.ExternalAccountRequestAssembler;
import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.domain.user.User;
import com.mybank.atmweb.domain.account.AccountFactory;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.dto.account.request.AccountOpenRequestDto;
import com.mybank.atmweb.dto.account.response.ExternalAccountOpenResponseDto;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.domain.account.AccountRepository;
import com.mybank.atmweb.repository.UserRepository;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalAccountService {
    private final ExternalBankClient externalBankClient;
    private final ExternalAccountRequestAssembler assembler;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public AccountOpenSummary externalAccountOpen(AccountOpenRequestDto dto, Long userId) {
        // 요청 DTO 조립
        ExternalOpenAccountRequestDto request = assembler.toExternalRequest(dto, userId);

        //외부 은행 호출
        ApiResponse<ExternalAccountOpenResponseDto> response = externalBankClient.createAccount(request);
        ExternalAccountOpenResponseDto data = response.getData();

        //User 조회
        User user = userRepository.findById(data.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //AccountFactory로 엔티티 생성
        Account externalAccount = AccountFactory.fromExternalResponse(user, data);

        //영속화
        accountRepository.save(externalAccount);

        return new AccountOpenSummary(response.getCode(), response.getMessage());
    }
}

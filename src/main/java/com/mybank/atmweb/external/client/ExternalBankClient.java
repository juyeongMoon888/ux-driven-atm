package com.mybank.atmweb.external.client;

import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.domain.verification.VerificationResult;
import com.mybank.atmweb.dto.account.response.ExternalAccountOpenResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalBankClient {
    private final RestTemplate restTemplate;

    public ApiResponse<ExternalAccountOpenResponseDto> createAccount(ExternalOpenAccountRequestDto dto) {
        String url = "http://localhost:8081/api/external-bank/open-account";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ExternalOpenAccountRequestDto> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<ApiResponse<ExternalAccountOpenResponseDto>> res =
                restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
                });

        return res.getBody();
    }

    public VerificationResult doVerifyAccount(AccountVerifyRequestDto dto) {
        String url = "http://localhost:8081/api/external-bank/transfer/verify";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AccountVerifyRequestDto> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<VerificationResult> res =
                restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
                });

        return res.getBody();
    }

    public ExAccDepositRes deposit(ExAccDepositReq req){
        String url = "http://localhost:8081/api/external-bank/deposit";
        return restTemplate.postForObject(url, req, ExAccDepositRes.class);
    }

    public ExAccWithdrawRes withdraw(ExAccWithdrawReq req){
        String url = "http://localhost:8081/api/external-bank/withdraw";
        return restTemplate.postForObject(url, req, ExAccWithdrawRes.class);
    }

    public ExAccConfirmRes confirm(ExAccConfirmReq req){
        String url = "http://localhost:8081/api/external-bank/confirm";
        return restTemplate.postForObject(url, req, ExAccConfirmRes.class);
    }

    public ExAccCancelRes cancel(ExAccCancelReq req){
        String url = "http://localhost:8081/api/external-bank/cancel";
        return restTemplate.postForObject(url, req, ExAccCancelRes.class);
    }
}

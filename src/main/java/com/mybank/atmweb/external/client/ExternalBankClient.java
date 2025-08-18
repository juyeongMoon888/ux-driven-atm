package com.mybank.atmweb.external.client;

import com.mybank.atmweb.dto.ApiResponse;
import com.mybank.atmweb.dto.ExternalAccountOpenResponseDto;
import com.mybank.atmweb.dto.ExternalOpenAccountRequestDto;
import com.mybank.atmweb.external.dto.ExternalAccountVerifyResponse;
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
        log.info("ExternalBankClient 진입");
        String url = "http://localhost:8081/api/external-bank/open-account";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ExternalOpenAccountRequestDto> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<ApiResponse<ExternalAccountOpenResponseDto>> ress =
                restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
                });

        return ress.getBody();

    }

    public ExternalAccountVerifyResponse verifyAccount(String bankType, String accountNumber) {
        String url = "http://localhost:8081/api/external-bank/account/validate?bankType=" + bankType + "&accountNumber=" + accountNumber;
        return restTemplate.getForObject(url, ExternalAccountVerifyResponse.class);
    }
}

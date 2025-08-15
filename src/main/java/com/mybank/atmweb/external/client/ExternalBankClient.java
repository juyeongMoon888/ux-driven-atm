package com.mybank.atmweb.external.client;

import com.mybank.atmweb.dto.AccountOpenRequestDto;
import com.mybank.atmweb.dto.ExternalAccountOpenResponse;
import com.mybank.atmweb.dto.ExternalOpenAccountRequestDto;
import com.mybank.atmweb.dto.UserProfile;
import com.mybank.atmweb.external.dto.ExternalAccountVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ExternalBankClient {
    private final RestTemplate restTemplate;

    public ExternalAccountOpenResponse createAccount(ExternalOpenAccountRequestDto dto) {
        String url = "http://localhost:8081/api/external-bank/open-account";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ExternalOpenAccountRequestDto> entity = new HttpEntity<>(dto, headers);
        ResponseEntity<ExternalAccountOpenResponse> res =
                restTemplate.exchange(url, HttpMethod.POST, entity, ExternalAccountOpenResponse.class);
        return res.getBody();
    }

    public ExternalAccountVerifyResponse verifyAccount(String bankType, String accountNumber) {
        String url = "http://localhost:8081/api/external-bank/account/validate?bankType=" + bankType + "&accountNumber=" + accountNumber;
        return restTemplate.getForObject(url, ExternalAccountVerifyResponse.class);
    }
}

package com.mybank.atmweb.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ExternalBankClient {
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> verifyAccount(String bankCode, String accountNumber) {
        String url = "http://localhost:8081/api/account/validate?bankCode=" + bankCode + "&accountNumber=" + accountNumber;
        return restTemplate.getForObject(url, Map.class);
    }
}

package com.mybank.atmweb.domain.account;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AccountNumberGenerator {
    private final Random random = new Random();
    public String generate(String prefix) {
        String randomDigits = String.format("%08d", random.nextInt(100_100_100));
        return prefix + randomDigits;
    }
}

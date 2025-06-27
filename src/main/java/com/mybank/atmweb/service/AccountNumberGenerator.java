package com.mybank.atmweb.service;

import com.mybank.atmweb.domain.Bank;

import java.util.Random;

public class AccountNumberGenerator {

    public static String generate(Bank bank) {
        String prefix = bank.name();
        String number = String.format("013d", new Random().nextLong(), Long.MAX_VALUE % 1_000_000_000_0000L);
        return prefix + "-" + number;
    }
}

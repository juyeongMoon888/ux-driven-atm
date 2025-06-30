package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.Bank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banks")
public class BankController {
    @GetMapping
    public ResponseEntity<?> getSupportedBanks() {
        List<Map<String, String>> banks = Arrays.stream(Bank.values())
                .map(bank -> Map.of(
                                "code", bank.getCode(),
                                "name", bank.getDisplayName()
                        ))
                .toList();
        return ResponseEntity.ok(banks);
    }
}

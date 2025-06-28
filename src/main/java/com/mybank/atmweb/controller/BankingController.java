package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.LoginRequest;
import com.mybank.atmweb.service.ATMService;
import com.mybank.atmweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/atm/banking")
public class BankingController {

    private final ATMService atmService;
    private final UserService userService;

    @GetMapping("/balance")
    public ResponseEntity<Integer> getBalance(String accountNumber) {
        int balance = atmService.checkBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }
}

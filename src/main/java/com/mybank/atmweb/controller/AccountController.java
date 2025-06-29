package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.Account;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.repository.AccountRepository;
import com.mybank.atmweb.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountRepository accountRepository;

    @GetMapping("/my")
    public ResponseEntity<?> getMyAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getId();

        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보 없음");
        }
        Optional<Account> account = accountRepository.findFirstByUser_Id(id);

        if (account.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("계좌 없음");
        }

        return ResponseEntity.ok(Map.of(
                "accountNumber", account.get().getAccountNumber(),
                "balance", account.get().getBalance()
        ));
    }
}

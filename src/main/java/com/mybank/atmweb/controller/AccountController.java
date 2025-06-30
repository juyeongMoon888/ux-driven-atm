package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.Account;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.repository.AccountRepository;
import com.mybank.atmweb.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountRepository accountRepository;

    @GetMapping("/my")
    public ResponseEntity<?> getMyAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {


        //인증 자체를 검사 JWT 없음, 만료 등
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보 없음"); //이거 안해주면 404뜸
        }
        Long id = userDetails.getId();
        Optional<Account> account = accountRepository.findFirstByUser_Id(id);

        if (account.isEmpty()) {
            return ResponseEntity.ok(Map.of("accountExists", false));
        }

        return ResponseEntity.ok(Map.of(
                "accountNumber", account.get().getAccountNumber(),
                "balance", account.get().getBalance()
        ));
    }
    @Bean
    public ApplicationRunner printAllMappings(RequestMappingHandlerMapping mapping) {
        return args -> {
            mapping.getHandlerMethods()
                    .forEach((req, handler) -> {
                        System.out.println(req + " => " + handler);
                    });
        };
    }
}

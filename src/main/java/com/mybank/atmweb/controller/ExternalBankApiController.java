package com.mybank.atmweb.controller;

import com.mybank.atmweb.dto.AccountOpenSummary;
import com.mybank.atmweb.dto.account.request.AccountOpenRequestDto;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import com.mybank.atmweb.security.CustomUserDetails;
import com.mybank.atmweb.service.ExternalAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/external-bank")
@RestController
public class ExternalBankApiController {
    private final ExternalAccountService externalAccountService;

    @PostMapping("/open-account")
    public ResponseEntity<?> openExternalAccount(
            @RequestBody AccountOpenRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getId();
        AccountOpenSummary response = externalAccountService.externalAccountOpen(dto, userId);
        return ResponseEntity.ok(response);
    }
}

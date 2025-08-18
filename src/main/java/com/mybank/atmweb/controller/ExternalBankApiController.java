package com.mybank.atmweb.controller;

import com.mybank.atmweb.assembler.ExternalAccountRequestAssembler;
import com.mybank.atmweb.dto.AccountOpenRequestDto;
import com.mybank.atmweb.dto.ExternalAccountOpenResponseDto;
import com.mybank.atmweb.dto.ExternalOpenAccountRequestDto;
import com.mybank.atmweb.dto.OperationSummary;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.security.CustomUserDetails;
import com.mybank.atmweb.service.ExternalAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/external-bank")
@RestController
public class ExternalBankApiController {
    private final ExternalBankClient externalBankClient;
    private final ExternalAccountRequestAssembler assembler;

    private final ExternalAccountService externalAccountService;

    @PostMapping("/open-account")
    public ResponseEntity<?> openExternalAccount(
            @RequestBody AccountOpenRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails user) {

        Long userId = user.getId();

        OperationSummary response = externalAccountService.externalAccountOpen(dto, userId);

        return ResponseEntity.ok(response);
    }
}

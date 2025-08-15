package com.mybank.atmweb.controller;

import com.mybank.atmweb.auth.JwtUtil;
import com.mybank.atmweb.dto.AccountOepnRequestDto;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.global.ResponseUtil;
import com.mybank.atmweb.global.code.SuccessCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/external-bank")
@RestController
public class ExternalBankApiController {
    private final JwtUtil jwtUtil;
    private final ResponseUtil responseUtil;
    private final ExternalBankClient externalBankClient;

    @PostMapping("/open-account")
    public ResponseEntity<?> openExternalAccount(@RequestBody AccountOepnRequestDto dto) {
        externalBankClient.createAccount(dto);
        return responseUtil.buildResponse(SuccessCode.ACCOUNT_CREATED, HttpStatus.OK, null);
    }

}

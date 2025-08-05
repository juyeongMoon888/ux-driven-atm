package com.mybank.atmweb.controller;

import com.mybank.atmweb.auth.JwtUtil;
import com.mybank.atmweb.dto.AccountRequestDto;
import com.mybank.atmweb.global.ResponseUtil;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/bank")
@RestController
public class AccountApiController {

    private final AccountService accountService;
    private final JwtUtil jwtUtil;
    private final ResponseUtil responseUtil;

    @PostMapping("/open-account")
    public ResponseEntity<?> createAccount(@RequestBody AccountRequestDto dto, HttpServletRequest request) {
        String token = jwtUtil.extractToken(request);
        Long userId = jwtUtil.getUserId(token);

        accountService.createAccount(userId, dto);

        return responseUtil.buildResponse(SuccessCode.ACCOUNT_CREATED, HttpStatus.OK, null);
    }
}

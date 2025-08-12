package com.mybank.atmweb.controller;

import com.mybank.atmweb.auth.JwtUtil;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.global.ResponseUtil;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.service.AccountService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/account-list")
    public ResponseEntity<?> accountList(HttpServletRequest request) {
        try {
            String token = jwtUtil.extractToken(request);
            Long userId = jwtUtil.getUserId(token);

            List<AccountSummaryDto> accountList = accountService.getAccountSummariesByUserId(userId);

            return responseUtil.buildResponse(SuccessCode.READ_SUCCESS, HttpStatus.OK, accountList);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferDto dto, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractToken(request);
            Long userId = jwtUtil.getUserId(token);

            accountService.updateBalance(dto, userId);

            return responseUtil.buildResponse(SuccessCode.UPDATE_SUCCESS, HttpStatus.OK, null);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    @GetMapping("/account-history")
    public ResponseEntity<?> accountHistoryList(@RequestParam String accountNumber, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractToken(request);
            Long userId = jwtUtil.getUserId(token);

            List<TransactionSummaryDto> transactionList = accountService.getTransactionByAccountId(accountNumber, userId);

            return responseUtil.buildResponse(SuccessCode.READ_SUCCESS, HttpStatus.OK, transactionList);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    @GetMapping("/account-history/{transactionId}")
    public ResponseEntity<?> getHistoryDetailApi(@PathVariable Long transactionId, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractToken(request);
            Long userId = jwtUtil.getUserId(token);

            TransactionDetailSummaryDto transactionDetail = accountService.getHistoryDetail(transactionId, userId);

            return responseUtil.buildResponse(SuccessCode.READ_SUCCESS, HttpStatus.OK, transactionDetail);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    @PatchMapping("/account-history/{transactionId}/memo")
    public ResponseEntity<?> updateTransactionMemo(
            @PathVariable Long transactionId,
            @RequestBody MemoUpdateRequest memoRequest,
            HttpServletRequest request
    ) {
        try {
            String token = jwtUtil.extractToken(request);
            Long userId = jwtUtil.getUserId(token);

            String accountNumber = accountService.updateTransactionMemo(transactionId, userId, memoRequest);

            return responseUtil.buildResponse(SuccessCode.UPDATE_SUCCESS, HttpStatus.OK, Map.of("accountNumber", accountNumber));
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }
}

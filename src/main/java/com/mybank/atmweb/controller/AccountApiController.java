package com.mybank.atmweb.controller;

import com.mybank.atmweb.application.AccountQueryService;
import com.mybank.atmweb.application.TransactionCommandService;
import com.mybank.atmweb.application.TransactionQueryService;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.dto.account.request.AccountOpenRequestDto;
import com.mybank.atmweb.external.dto.ExternalAccountVerifyResponse;
import com.mybank.atmweb.global.ResponseUtil;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.security.CustomUserDetails;
import com.mybank.atmweb.service.AccountService;
import com.mybank.atmweb.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/bank")
@RestController
public class AccountApiController {

    private final AccountService accountService;
    private final ResponseUtil responseUtil;
    private final TransferService transferService;
    private final TransactionQueryService transactionQueryService;
    private final TransactionCommandService transactionCommandService;
    private final AccountQueryService accountQueryService;

    @PostMapping("/open-account")
    public ResponseEntity<?> openInternalAccount(@RequestBody AccountOpenRequestDto dto,
                                                 @AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getId();
        accountService.createAccount(userId, dto);
        return responseUtil.buildResponse(SuccessCode.ACCOUNT_CREATED, HttpStatus.OK, null);
    }

    @GetMapping("/account-list")
    public ResponseEntity<?> accountList(@AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getId();
        List<AccountSummaryDto> accountList = accountQueryService.getListByOwner_Id(userId);
        return responseUtil.buildResponse(SuccessCode.READ_SUCCESS, HttpStatus.OK, accountList);
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody TransferDto dto,
                                     @AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getId();
        accountService.handleDepositWithdraw(dto, userId);
        return responseUtil.buildResponse(SuccessCode.UPDATE_SUCCESS, HttpStatus.OK, null);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody TransferDto dto,
                                      @AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getId();
        accountService.handleDepositWithdraw(dto, userId);
        return responseUtil.buildResponse(SuccessCode.UPDATE_SUCCESS, HttpStatus.OK, null);
    }

    @GetMapping("/account-history")
    public ResponseEntity<?> accountHistoryList(@RequestParam String accountNumber,
                                                @AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getId();
        List<TransactionSummaryDto> transactionHistory = transactionQueryService.getTransactionHistory(accountNumber, userId);
        return responseUtil.buildResponse(SuccessCode.READ_SUCCESS, HttpStatus.OK, transactionHistory);
    }

    @GetMapping("/account-history/{transactionId}")
    public ResponseEntity<?> getHistoryDetailApi(@PathVariable Long transactionId,
                                                 @AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getId();
        TransactionDetailSummaryDto transactionDetail = transactionQueryService.getTransactionHistoryDetail(transactionId, userId);
        log.info("transactionDetail={}", transactionDetail.getTransfer());
        return responseUtil.buildResponse(SuccessCode.READ_SUCCESS, HttpStatus.OK, transactionDetail);
    }

    @PatchMapping("/account-history/{transactionId}/memo")
    public ResponseEntity<?> updateTransactionMemo(
            @PathVariable Long transactionId,
            @RequestBody MemoUpdateRequest memoRequest,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getId();
        String accountNumber = transactionCommandService.updateTransactionMemo(transactionId, userId, memoRequest);
        return responseUtil.buildResponse(SuccessCode.UPDATE_SUCCESS, HttpStatus.OK, Map.of("accountNumber", accountNumber));
    }

    @PostMapping("/transfer/verify-external")
    public ResponseEntity<?> validateExternalAccount(@Valid @RequestBody ExternalAccountVerifyRequest dto) {
        ExternalAccountVerifyResponse result = transferService.verifyExternalAccount(dto);
        return ResponseEntity.ok(result);
    }
}

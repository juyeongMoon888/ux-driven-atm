package com.mybank.atmweb.controller;

import com.mybank.atmweb.application.query.AccountQueryService;
import com.mybank.atmweb.application.command.TransactionCommandService;
import com.mybank.atmweb.application.query.TransactionQueryService;
import com.mybank.atmweb.domain.verification.VerificationCode;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.dto.account.AccountOptionDto;
import com.mybank.atmweb.domain.verification.VerificationResult;
import com.mybank.atmweb.dto.account.request.AccountOpenRequestDto;
import com.mybank.atmweb.dto.transfer.TransferRequestDto;
import com.mybank.atmweb.global.ResponseUtil;
import com.mybank.atmweb.global.code.BaseCode;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.security.CustomUserDetails;
import com.mybank.atmweb.service.AccountService;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import com.mybank.atmweb.service.transfer.model.TransferRouter;
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
    private final TransactionQueryService transactionQueryService;
    private final TransactionCommandService transactionCommandService;
    private final AccountQueryService accountQueryService;
    private final TransferRouter transferRouter;


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
    public ResponseEntity<?> deposit(@RequestBody DepositRequestDto dto,
                                     @AuthenticationPrincipal CustomUserDetails user,
                                     @RequestHeader("Idempotency-Key") String idempotencyKey) {
        Long userId = user.getId();
        OperationSummary summary = transferRouter.routeAndExecute(dto, userId, idempotencyKey);
        return responseUtil.buildResponse(SuccessCode.UPDATE_SUCCESS, HttpStatus.OK, summary);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequestDto dto,
                                      @AuthenticationPrincipal CustomUserDetails user,
                                      @RequestHeader("Idempotency-Key") String idempotencyKey) {
        Long userId = user.getId();
        OperationSummary summary = transferRouter.routeAndExecute(dto, userId, idempotencyKey);
        return responseUtil.buildResponse(SuccessCode.UPDATE_SUCCESS, HttpStatus.OK, summary);
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

    @GetMapping("/account-options")
    public ResponseEntity<?> getAccountOptions(@AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getId();
        List<AccountOptionDto> accountOptions = accountQueryService.getOptionsByOwnerId(userId);
        return responseUtil.buildResponse(SuccessCode.READ_SUCCESS, HttpStatus.OK, accountOptions);
    }

    @PostMapping("/transfer/verify")
    public ResponseEntity<?> validateExternalAccount(@Valid @RequestBody AccountVerifyRequestDto dto) {
        VerificationResult result = accountService.verifyAccount(dto);
        BaseCode baseCode = mapToBaseCode(result.getCode());

        return responseUtil.buildResponse(baseCode, baseCode.getHttpStatus());
    }

    private BaseCode mapToBaseCode(VerificationCode code) {
        return switch (code) {
            case OK -> SuccessCode.ACCOUNT_VERIFIED;
            case ACCOUNT_NOT_FOUND -> ErrorCode.ACCOUNT_NOT_FOUND;
            case ACCOUNT_FROZEN -> ErrorCode.ACCOUNT_FROZEN;
            case ACCOUNT_CLOSED -> ErrorCode.ACCOUNT_CLOSED;
            case VERIFICATION_FAILED -> ErrorCode.VERIFICATION_FAILED;
        };
    }

    @PostMapping("/transfer")
    public ResponseEntity<OperationSummary> transfer(@AuthenticationPrincipal CustomUserDetails user,
                                                     @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                     @RequestBody TransferRequestDto dto) {
        Long userId = user.getId();
        OperationSummary summary = transferRouter.routeAndExecute(dto, userId, idempotencyKey);
        return ResponseEntity.ok(summary);
    }
}


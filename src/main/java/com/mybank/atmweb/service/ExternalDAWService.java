package com.mybank.atmweb.service;

import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.domain.Idempotency;
import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.domain.account.AccountRepository;
import com.mybank.atmweb.domain.transaction.Transactions;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.IdempotencyRepository;
import com.mybank.atmweb.repository.TransactionRepository;
import com.mybank.atmweb.service.transfer.model.OperationContext;
import com.mybank.atmweb.service.transfer.model.OperationSummary;
import com.mybank.atmweb.service.transfer.model.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

@Service
@RequiredArgsConstructor
public class ExternalDAWService {

    private final ExternalBankClient externalBankClient;
    private final TransactionRepository txRepo;
    private final AccountRepository accRepo;
    private final IdempotencyRepository idemRepo;

    public OperationSummary externalDeposit(OperationContext ctx) {
        // 0) 멱등키 선확인
        if (idemRepo.existsByKey(ctx.getIdempotencyKey())) {
            Transactions existing = txRepo.findByIdempotencyKeyAndOperationType(ctx.getIdempotencyKey(), OperationType.DEPOSIT)
                    .orElseThrow(() -> new CustomException(ErrorCode.IDEMPOTENCY_KEY_NOT_FOUND));

            return new OperationSummary(
                    SuccessCode.TRANSFER_OK.name(),
                    SuccessCode.TRANSFER_OK.getMessageKey(),
                    TransactionStatus.COMPLETED,
                    existing.getId());
        }

        ExAccDepositReq dreq = ExAccDepositReq.fromDeposit(ctx);

        // 1) 외부 입금 요청
        ExAccDepositRes dres;
        try {
            dres = externalBankClient.deposit(dreq);
        } catch (HttpStatusCodeException ex) {
            return new OperationSummary(
                    "UPSTREAM_ERROR",
                    "external.deposit.upstream_error",
                    TransactionStatus.FAILED,
                    null
            );
        } catch (Exception ex) {
            return new OperationSummary(
                    "UPSTREAM_UNREACHABLE",
                    "external.deposit.unreachable",
                    TransactionStatus.FAILED,
                    null
            );
        }
        if (!dres.isSuccess()) {
            return new OperationSummary(
              dres.getCode(),
              dres.getMessage(),
              TransactionStatus.FAILED,
              null
            );
        }

        // 2) 외부 입금 계좌 잠금 조회
        Account acc = accRepo.findByAccountNumberAndOwner_Id(ctx.getToAccountNumber(), ctx.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        // 3) 입금 적용
        long before = acc.getBalance();
        acc.deposit(ctx.getAmount());

        // 4) 트랜잭션 적용
        Transactions tx = txRepo.save(
                Transactions.builder()
                        .account(acc)
                        .operationType(OperationType.DEPOSIT)
                        .amount(ctx.getAmount())
                        .balanceBefore(before)
                        .balanceAfter(acc.getBalance())
                        .memo(ctx.getMemo())
                        .toAccountNumber(ctx.getToAccountNumber())
                        .toBank(ctx.getToBank())
                        .transactionStatus(TransactionStatus.COMPLETED)
                        .idempotencyKey(ctx.getIdempotencyKey())
                        .externalTxId(dres.getExTxId())
                        .externalBank(BankType.valueOf(dres.getExternalBank()))
                        .build()
        );

        return new OperationSummary(
                dres.getCode(),
                dres.getMessage(),
                TransactionStatus.COMPLETED,
                null);
    }

    public OperationSummary externalWithdraw(OperationContext ctx) {
        ExAccWithdrawReq wreq = ExAccWithdrawReq.fromWithdraw(ctx);
        ExAccWithdrawRes wres;

        try {
            wres = externalBankClient.withdraw(wreq);
        } catch (HttpStatusCodeException ex) {
            return new OperationSummary(
                    "UPSTREAM_ERROR",
                    "external.withdraw.upstream_error",
                    TransactionStatus.FAILED,
                    null
            );
        } catch (Exception ex) {
            return new OperationSummary(
                    "UPSTREAM_UNREACHABLE",
                    "external.withdraw.unreachable",
                    TransactionStatus.FAILED,
                    null
            );
        }

        if (!wres.isApproved()) {
            return new OperationSummary(
                    wres.getCode(),
                    wres.getMessage(),
                    TransactionStatus.FAILED,
                    null);
        }
        return new OperationSummary(
                wres.getCode(),
                wres.getMessage(),
                TransactionStatus.COMPLETED,
                null);
    }
}

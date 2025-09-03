package com.mybank.atmweb.dto;

import com.mybank.atmweb.service.transfer.model.OperationContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExAccWithdrawReq {
    private String toBank;
    private String toAccountNumber;
    private Long amount;
    private String memo;
    private Long exTxId;
    private String idempotencyKey;
    private String fromBank;
    private String fromAccountNumber;

    public static ExAccWithdrawReq fromTransfer(OperationContext ctx, Long exTxId) {
        return ExAccWithdrawReq.builder()
                .toBank(ctx.getToBank())
                .toAccountNumber(ctx.getToAccountNumber())
                .amount(ctx.getAmount())
                .memo(ctx.getMemo())
                .exTxId(exTxId)
                .idempotencyKey(ctx.getIdempotencyKey())
                .fromBank(ctx.getFromBank())
                .fromAccountNumber(ctx.getFromAccountNumber())
                .build();
    }

    public static ExAccWithdrawReq fromWithdraw(OperationContext ctx) {
        return ExAccWithdrawReq.builder()
                .fromBank(ctx.getToBank())
                .fromAccountNumber(ctx.getToAccountNumber())
                .amount(ctx.getAmount())
                .memo(ctx.getMemo())
                .idempotencyKey(ctx.getIdempotencyKey())
                .build();
    }
}

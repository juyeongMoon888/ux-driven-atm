package com.mybank.atmweb.service.transfer.model;

import com.mybank.atmweb.domain.FlowContext;
import com.mybank.atmweb.domain.Idempotency;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class OperationContext {
    private final Long userId;
    private final String fromAccountNumber; // WITHDRAW/TRANSFER 에서 사용
    private final String toAccountNumber; // DEPOSIT/TRANSFER 에서 사용
    private final String fromBank;
    private final String toBank;
    private final long amount;
    private final String memo;
    private final String idempotencyKey;
    private final FlowContext flow;
}

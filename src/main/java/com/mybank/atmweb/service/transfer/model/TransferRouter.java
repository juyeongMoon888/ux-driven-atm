package com.mybank.atmweb.service.transfer.model;

import com.mybank.atmweb.application.query.AccountQueryService;
import com.mybank.atmweb.domain.DawFlow;
import com.mybank.atmweb.domain.FlowContext;
import com.mybank.atmweb.domain.FlowType;
import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.dto.DepositRequestDto;
import com.mybank.atmweb.dto.WithdrawRequestDto;
import com.mybank.atmweb.dto.transfer.TransferRequestDto;
import com.mybank.atmweb.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransferRouter {
    private final AccountQueryService accountQueryService;

    // 단순 입출금
    private final InternalDAWService internalDAWService;
    private final ExternalDAWService externalDAWService;

    // 이체
    private final InternalTransferService internalTransferService;
    private final OutboundToExternalOrchestrator outboundToExternalOrchestrator;
    private final ExternalInboundOrchestrator externalInboundOrchestrator;
    private final ExternalToExternalOrchestrator externalToExternalOrchestrator;

    private final IdempotencyService idempotencyService;

    public OperationSummary routeAndExecute(TransferRequestDto dto, long userId, String idempotencyKey) {
        Account fromAcc = accountQueryService.findAccountByAccountNumberAndUserId(dto.getFromAccountNumber(), userId);
        String fromBank = fromAcc.getBank().name();
        String toBank = dto.getToBank();

        // 컨텍스트 구성 (멱등키 생성)
        OperationContext ctx = OperationContext.builder()
                .fromBank(fromBank)
                .fromAccountNumber(dto.getFromAccountNumber())
                .toBank(toBank)
                .toAccountNumber(dto.getToAccountNumber())
                .amount(dto.getAmount())
                .memo(dto.getMemo())
                .userId(userId)
                .idempotencyKey(idempotencyService.registerOrGet(idempotencyKey).toString())
                .flow(FlowContext.TRANSFER)
                .build();

        // 플로우 판정
        FlowType flow = decideFlow(fromBank, toBank);

        // 라우팅
        return switch (flow) {
            case INTERNAL_TO_INTERNAL -> internalTransferService.transferInternal(ctx);
            case INTERNAL_TO_EXTERNAL -> outboundToExternalOrchestrator.outbound(ctx);
            case EXTERNAL_TO_INTERNAL -> externalInboundOrchestrator.inboundToMybank(ctx);
            case EXTERNAL_TO_EXTERNAL -> externalToExternalOrchestrator.relay(ctx);
        };
    }

    //단순 deposit 전용 내/외부
    public OperationSummary routeAndExecute(DepositRequestDto dto, long userId, String idempotencyKey) {
        OperationContext ctx = OperationContext.builder()
                .userId(userId)
                .toAccountNumber(dto.getAccountNumber())
                .toBank(dto.getBank())
                .amount(dto.getAmount())
                .memo(dto.getMemo())
                .idempotencyKey(idempotencyKey)
                .flow(FlowContext.SIMPLE)
                .build();

        DawFlow flow = decideFlow(dto.getBank());

        return switch (flow) {
            case INTERNAL -> internalDAWService.internalDeposit(ctx);
            case EXTERNAL -> externalDAWService.externalDeposit(ctx);
        };
    }
    //단순 withdraw 전용 내/외부
    public OperationSummary routeAndExecute(WithdrawRequestDto dto, long userId, String idempotencyKey) {
        OperationContext ctx = OperationContext.builder()
                .userId(userId)
                .fromAccountNumber(dto.getAccountNumber())
                .fromBank(dto.getBank())
                .amount(dto.getAmount())
                .memo(dto.getMemo())
                .idempotencyKey(idempotencyKey)
                .flow(FlowContext.SIMPLE)
                .build();

        DawFlow flow = decideFlow(dto.getBank());

        return switch (flow) {
            case INTERNAL -> internalDAWService.internalWithdraw(ctx);
            case EXTERNAL -> externalDAWService.externalWithdraw(ctx);
        };
    }

    private FlowType decideFlow(String fromBank, String toBank) {
        boolean fromMy = "MYBANK".equals(fromBank);
        boolean toMy = "MYBANK".equals(toBank);
        if (fromMy && toMy) return FlowType.INTERNAL_TO_INTERNAL;
        if (fromMy) return FlowType.INTERNAL_TO_EXTERNAL;
        if (toMy) return FlowType.EXTERNAL_TO_INTERNAL;
        return FlowType.EXTERNAL_TO_INTERNAL;
    }

    private DawFlow decideFlow(String bank) {
        if (bank.equals("MYBANK"))
            return DawFlow.INTERNAL;
        else return DawFlow.EXTERNAL;
    }
}

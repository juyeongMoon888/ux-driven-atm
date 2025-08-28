package com.mybank.atmweb.service.transfer;

import com.mybank.atmweb.application.command.TransactionCommandService;
import com.mybank.atmweb.application.query.AccountQueryService;
import com.mybank.atmweb.application.query.TransactionQueryService;
import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.domain.account.AccountRepository;
import com.mybank.atmweb.domain.transaction.Transactions;
import com.mybank.atmweb.domain.user.User;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.UserRepository;
import com.mybank.atmweb.service.AccountService;
import com.mybank.atmweb.service.transfer.model.OperationContext;
import com.mybank.atmweb.service.transfer.model.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalTransferExecutor implements TransferExecutor {

    private final TransactionCommandService transactionCommandService;
    private final AccountService accountService;
    private final ExternalBankClient externalBankClient;
    private final UserRepository userRepository;
    private final TransactionQueryService transactionQueryService;

    @Override
    public boolean supports(String bank) {
        return BankType.valueOf(bank).isInternal();
    }

    @Override
    public OperationSummary execute(OperationContext ctx) {
        OperationType operationType = ctx.getOperationType();
        return switch (operationType) {
            case DEPOSIT -> {
                accountService.deposit(ctx.getToAccountNumber(), ctx.getAmount(), ctx.getMemo(), ctx.getUserId());
                yield new OperationSummary(SuccessCode.DEPOSIT_OK.name(), SuccessCode.DEPOSIT_OK.getMessageKey());
            }
            case WITHDRAW -> {
                accountService.withdraw(ctx.getFromAccountNumber(), ctx.getAmount(), ctx.getMemo(), ctx.getUserId());
                yield new OperationSummary(SuccessCode.WITHDRAW_OK.name(), SuccessCode.WITHDRAW_OK.getMessageKey());
            }
            case TRANSFER -> {
                OperationSummary res = handleTransfer(ctx);
                yield new OperationSummary(res.getCode(), res.getMessage());
            }
        };
    }

    private OperationSummary handleTransfer(OperationContext ctx) {
        OperationSummary summary;
        if (ctx.getToBank().equals("MYBANK")) {
            // 내부->내부 은행 송금
            accountService.transferInternal(ctx.getFromAccountNumber(), ctx.getUserId(),
                                            ctx.getToAccountNumber(), ctx.getAmount(), ctx.getMemo());
            summary = new OperationSummary(SuccessCode.TRANSFER_OK.name(), SuccessCode.TRANSFER_OK.getMessageKey());
        } else {
            Long txId = transactionCommandService.createPendingWithdraw(ctx);

            ExAccOperationReq dto = new ExAccOperationReq(
                    OperationType.DEPOSIT.toString(),
                    ctx.getFromBank(),
                    ctx.getFromAccountNumber(),
                    ctx.getToBank(),
                    ctx.getToAccountNumber(),
                    ctx.getAmount(),
                    ctx.getMemo(),
                    ctx.getUserId(),
                    txId
            );

            //외부 은행 호출
            ApiResponse<ExAccOperationRes> res = externalBankClient.deposit(dto);
            ExAccOperationRes data = res.getData();

            //User 조회
            User user = userRepository.findById(data.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            //Transactions 조회
            Transactions tx = transactionQueryService.getTransactionOrThrow(data.getTxId(), user.getId());

            //transaction 영속화
            transactionCommandService.save(Transactions.builder().
                    transactionStatus(TransactionStatus.COMPLETED)
                    .build());
            summary = new OperationSummary(res.getCode(), res.getMessage());
        }
        return summary;
    }
}

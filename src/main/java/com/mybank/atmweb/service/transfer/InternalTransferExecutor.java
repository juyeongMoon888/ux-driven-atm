package com.mybank.atmweb.service.transfer;

import com.mybank.atmweb.application.command.TransactionCommandService;
import com.mybank.atmweb.application.query.AccountQueryService;
import com.mybank.atmweb.application.query.TransactionQueryService;
import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.domain.account.AccountRepository;
import com.mybank.atmweb.domain.transaction.Transaction;
import com.mybank.atmweb.domain.user.User;
import com.mybank.atmweb.dto.*;
import com.mybank.atmweb.external.client.ExternalBankClient;
import com.mybank.atmweb.global.code.ErrorCode;
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
    private final AccountQueryService accountQueryService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionQueryService transactionQueryService;

    @Override
    public boolean supports(String bank) {
        return BankType.valueOf(bank).isInternal();
    }

    @Override
    public void execute(OperationContext ctx) {
        OperationType operationType = ctx.getOperationType();
        switch (operationType) {
            case DEPOSIT ->
                    accountService.deposit(ctx.getToAccountNumber(), ctx.getAmount(), ctx.getMemo(), ctx.getUserId());
            case WITHDRAW ->
                    accountService.withdraw(ctx.getFromAccountNumber(), ctx.getAmount(), ctx.getMemo(), ctx.getUserId());
            case TRANSFER -> handleTransfer(ctx);
        }
    }

    private OperationSummary handleTransfer(OperationContext ctx) {
        if (ctx.getToBank().equals("MYBANK")) {
            // 내부->내부 은행 송금
            accountService.transferInternal(ctx.getFromAccountNumber(), ctx.getUserId(),
                                            ctx.getToAccountNumber(), ctx.getAmount(), ctx.getMemo());
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

            //Transaction 조회
            Transaction tx = transactionQueryService.getTransactionOrThrow(data.getTxId(), user.getId());

            //transaction 영속화
            transactionCommandService.save(Transaction.builder().
                    transactionStatus(data.getStatus())
                    .build());

            return new OperationSummary(res.getCode(), res.getMessage());
        }
    }
}

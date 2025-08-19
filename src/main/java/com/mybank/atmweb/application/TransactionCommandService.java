package com.mybank.atmweb.application;

import com.mybank.atmweb.domain.transaction.Transaction;
import com.mybank.atmweb.dto.MemoUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionCommandService {

    private final TransactionQueryService transactionQueryService;

    @Transactional
    public String updateTransactionMemo(Long transactionId, Long userId, MemoUpdateRequest memoRequest) {
        String memo = memoRequest.getMemo();
        Transaction tx = transactionQueryService.getTransactionOrThrow(transactionId, userId);
        tx.setMemo(memo);
        return tx.getAccount().getAccountNumber();
    }
}

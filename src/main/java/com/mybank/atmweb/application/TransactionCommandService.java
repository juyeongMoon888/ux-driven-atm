package com.mybank.atmweb.application;

import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.domain.transaction.Transaction;
import com.mybank.atmweb.dto.MemoUpdateRequest;
import com.mybank.atmweb.dto.TransferDto;
import com.mybank.atmweb.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionCommandService {

    private final TransactionQueryService transactionQueryService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public String updateTransactionMemo(Long transactionId, Long userId, MemoUpdateRequest memoRequest) {
        String memo = memoRequest.getMemo();
        Transaction tx = transactionQueryService.getTransactionOrThrow(transactionId, userId);
        tx.setMemo(memo);
        return tx.getAccount().getAccountNumber();
    }

    @Transactional
    public void recordTransaction(Account account,
                                   Long balanceBefore,
                                   Long balanceAfter,
                                   TransferDto dto) {
        Transaction tx = new Transaction(account, dto.getType(), dto.getAmount(), balanceBefore, balanceAfter, dto.getMemo());

        transactionRepository.save(tx);
    }
}

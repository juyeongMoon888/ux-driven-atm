package com.mybank.atmweb.application;

import com.mybank.atmweb.domain.transaction.Transaction;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction getTransactionOrThrow(Long transactionId, Long userId) {
        return transactionRepository.findByIdAndAccount_Owner_Id(transactionId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_DETAIL_NOT_FOUND));
    }
}

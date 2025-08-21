package com.mybank.atmweb.application.query;

import com.mybank.atmweb.domain.transaction.Transaction;
import com.mybank.atmweb.dto.TransactionDetailSummaryDto;
import com.mybank.atmweb.dto.TransactionSummaryDto;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction getTransactionOrThrow(Long transactionId, Long userId) {
        return transactionRepository.findByIdAndAccount_Owner_Id(transactionId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_DETAIL_NOT_FOUND));
    }

    @Transactional
    public List<TransactionSummaryDto> getTransactionHistory(String accountNumber, Long userId) {
        return transactionRepository.findSummariesByAccountNumberAndUserId(accountNumber, userId);
    }

    @Transactional
    public TransactionDetailSummaryDto getTransactionHistoryDetail(Long transactionId, Long userId) {
        return transactionRepository.findHistoryDetailByTransactionIdAndUserId(transactionId, userId);
    }
}

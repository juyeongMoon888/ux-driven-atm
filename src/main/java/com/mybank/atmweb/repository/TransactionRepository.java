package com.mybank.atmweb.repository;

import com.mybank.atmweb.domain.transaction.Transaction;
import com.mybank.atmweb.dto.TransactionDetailSummaryDto;
import com.mybank.atmweb.dto.TransactionSummaryDto;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndAccount_Owner_Id(Long transactionId, Long userId);

    @Query("""
            select new com.mybank.atmweb.dto.TransactionSummaryDto(
                t.id, t.createdAt, t.transfer, t.amount, t.memo
            )
            from Transaction t
            where t.account.accountNumber = :accountNumber
            and t.account.owner.id = :userId
            order by t.createdAt desc
            """)
    List<TransactionSummaryDto> findSummariesByAccountNumberAndUserId(
            @Param("accountNumber") String accountNumber,
            @Param("userId") Long userId
    );

    @Query("""
            select new com.mybank.atmweb.dto.TransactionDetailSummaryDto(
                t.createdAt, t.transfer, t.amount, t.balanceAfter, t.memo
            )
            from Transaction t
            where t.id = :transactionId
            and t.account.owner.id = :userId
            """)
    TransactionDetailSummaryDto findHistoryDetailByTransactionIdAndUserId(
            @Param("transactionId") Long transactionId,
            @Param("userId") Long userId
    );
}

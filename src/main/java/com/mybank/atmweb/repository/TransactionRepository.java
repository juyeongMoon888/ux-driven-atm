package com.mybank.atmweb.repository;

import com.mybank.atmweb.domain.transaction.Transactions;
import com.mybank.atmweb.dto.TransactionDetailSummaryDto;
import com.mybank.atmweb.dto.TransactionSummaryDto;
import com.mybank.atmweb.service.transfer.model.OperationType;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transactions, Long> {

    Optional<Transactions> findByIdAndAccount_Owner_Id(Long transactionId, Long userId);

    @Query("""
            select new com.mybank.atmweb.dto.TransactionSummaryDto(
                t.id, t.createdAt, t.operationType, t.amount, t.memo
            )
            from Transactions t
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
                t.createdAt, t.operationType, t.amount, t.balanceAfter, t.memo
            )
            from Transactions t
            where t.id = :transactionId
            and t.account.owner.id = :userId
            """)
    TransactionDetailSummaryDto findHistoryDetailByTransactionIdAndUserId(
            @Param("transactionId") Long transactionId,
            @Param("userId") Long userId
    );

    Optional<Transactions> findByIdForUpdate(Long txId);

    Optional<Transactions> findMasterByIdempotencyKey(String idempotencyKey);

    Optional<Transactions> findMasterByIdempotencyKeyAndOperationType(String idempotencyKey, OperationType operationType);

    boolean existsByIdempotencyKeyAndOperationType(String idempotencyKey, OperationType operationType);

    Optional<Transactions> findByIdempotencyKey(String idempotencyKey);

    Optional<Transactions> findMasterByIdempotencyKeyForUpdate(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transactions t where t.id = :id and t.master = true")
    Optional<Transactions> findMasterByIdForUpdate(@Param("id") Long id);
}

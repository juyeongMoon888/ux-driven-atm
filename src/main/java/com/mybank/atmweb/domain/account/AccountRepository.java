package com.mybank.atmweb.domain.account;

import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.domain.AccountStatus;
import com.mybank.atmweb.dto.AccountSummaryDto;
import com.mybank.atmweb.dto.account.AccountOptionDto;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("""
            select new com.mybank.atmweb.dto.AccountSummaryDto(
                a.bank, a.accountNumber, a.balance
            )
            from Account a
            where a.owner.id = :userId
            """)
    List<AccountSummaryDto> findSummariesByOwnerId(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findByAccountNumberAndOwner_Id(String accountNumber, Long userId);

    @Query("""
            select new com.mybank.atmweb.dto.account.AccountOptionDto(
                a.bank, a.accountNumber
            )
            from Account a
            where a.owner.id = :userId
            """)
    List<AccountOptionDto> findOptionsByOwnerId(@Param("userId") Long userId);

    @Query("""
           select a.status
             from Account a
            where a.bank = :bank
              and a.accountNumber = :accountNumber
           """)
    Optional<AccountStatus> findStatusByBankAndAccountNumber(@Param("bank") BankType bank,
                                                             @Param("accountNumber") String accountNumber);

    @Query("select a.id from Account a where a.accountNumber = :accountNumber")
    Optional<Long> findIdByAccountNumber(@Param("accountNumber") String fromAccountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findByAccountNumber(String fromAccountNumber);
}


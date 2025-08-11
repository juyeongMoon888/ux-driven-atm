package com.mybank.atmweb.repository;

import com.mybank.atmweb.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Set<Transaction> findByAccount_AccountNumberAndAccount_Owner_IdOrderByCreatedAtDesc(String accountNumber, Long userId);
}

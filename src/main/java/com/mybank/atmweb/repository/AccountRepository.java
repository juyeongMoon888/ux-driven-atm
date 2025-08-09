package com.mybank.atmweb.repository;

import com.mybank.atmweb.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Set<Account> findByOwner_Id(Long userId);

    Optional<Account> findByAccountNumberAndOwner_Id(String accountNumber, Long userId);
}

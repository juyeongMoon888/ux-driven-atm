package com.mybank.atmweb.repository;

import com.mybank.atmweb.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}

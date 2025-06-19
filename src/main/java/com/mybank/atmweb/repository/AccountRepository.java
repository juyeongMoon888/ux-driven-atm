package com.mybank.atmweb.repository;

import com.mybank.atmweb.domain.Account;
import com.mybank.atmweb.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, User> {
    Optional<Account> findByUser(User user);
}

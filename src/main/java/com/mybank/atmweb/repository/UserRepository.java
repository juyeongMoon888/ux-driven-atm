package com.mybank.atmweb.repository;

import com.mybank.atmweb.domain.Account;
import com.mybank.atmweb.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByResidentNumber(String residentNumber);
    boolean existsByLoginId(String loginId);

    Optional<User> findByLoginId(String loginId);
}



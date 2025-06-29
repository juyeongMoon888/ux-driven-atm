package com.mybank.atmweb.repository;

import com.mybank.atmweb.domain.Account;
import com.mybank.atmweb.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, User> {
    Optional<Account> findByAccountNumber(String accountNumber); //단일 계좌 조회

    List<Account> findAllByUser(User user);//사용자 계좌 전체 조회
}

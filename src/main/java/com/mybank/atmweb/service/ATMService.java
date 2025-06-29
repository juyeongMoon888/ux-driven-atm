package com.mybank.atmweb.service;

import com.mybank.atmweb.domain.Account;
import com.mybank.atmweb.domain.Bank;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.repository.AccountRepository;
import com.mybank.atmweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ATMService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountNumberGenerator generator;

    public User login(String residentNumber, String password) {
        return userRepository.findByResidentNumber(residentNumber)
                .filter(user -> user.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException("로그인 실패"));
    }

    //잔액 조회
    public int checkBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("계좌 없음"));
        return account.getBalance();
    }

    //계좌 생성
    public String openAccount(User user, Bank bank) {
        String accountNumber = generator.generate(bank);

        Account account = new Account();
        account.setUser(user);
        account.setBank(bank);
        account.setAccountNumber(accountNumber);
        account.setBalance(0);

        accountRepository.save(account);
        return accountNumber;
    }


}

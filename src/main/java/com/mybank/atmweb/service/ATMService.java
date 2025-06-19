package com.mybank.atmweb.service;

import com.mybank.atmweb.domain.Account;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.repository.AccountRepository;
import com.mybank.atmweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ATMService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public User login(String residentNumber, String password) {
        return userRepository.findByResidentNumber(residentNumber)
                .filter(user -> user.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException("로그인 실패"));
    }

    //계좌 확인
    public int checkBalance(User user) {
        return accountRepository.findByUser(user)
                .map(Account::getBalance)
                .orElseThrow(() -> new IllegalArgumentException("계좌 없음"));
    }

    //입금
    public void deposit(User user, int amount) {
        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("계좌 없음"));
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
    }

    //출금
    public void withdraw(User user, int amount) {
        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("계좌 없음"));
        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("잔액 부족");
        }
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
    }
}

package com.mybank.atmweb.domain;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@Entity @Transactional
public class Account {
    @Id @GeneratedValue
    private Long id;

    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private Bank bank;

    private int balance;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;//관계의 주인, DB 반영

    public void deposit(int amount) {
        this.balance += amount;
    }

    public void withdraw(int amount) {
        if (amount > this.balance) {
            throw new IllegalArgumentException("잔액 부족");
        }
        this.balance -= amount;
    }

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();

}

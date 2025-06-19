package com.mybank.atmweb.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Transaction {
    @Id @GeneratedValue
    private Long id;

    private String type; // "DEPOSIT" or "WITHDRAW"
    private int amount;

    private LocalDateTime time;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
}

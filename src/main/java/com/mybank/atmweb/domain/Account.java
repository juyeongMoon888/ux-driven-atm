package com.mybank.atmweb.domain;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@Entity @Transactional
public class Account {

    @Id @GeneratedValue
    private Long id;

    private String accountNumber;
    private String accountName; //계좌명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    private String password;

    private Long balance;

    private LocalDateTime createAt;
}

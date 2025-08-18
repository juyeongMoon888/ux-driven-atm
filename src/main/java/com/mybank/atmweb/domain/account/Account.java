package com.mybank.atmweb.domain.account;

import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.domain.User;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Builder
@Getter
@Entity @Transactional
@AllArgsConstructor
public class Account {

    protected Account() {
    }
    @Column(updatable = false, nullable = false)
    @Id @GeneratedValue
    private Long id;

    private String accountNumber;
    private String accountName;

    @Column(name = "external_account_id")
    private String externalAccountId; //External일 때만 채움 (internal이면 null)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    private String password; //External이면 null

    private Long balance;

    @CreationTimestamp
    private LocalDateTime createAt;

    @Enumerated(EnumType.STRING)
    @Column(name="bank")
    private BankType bank;

    public void deposit(Long amount) {
        this.balance += amount;
    }

    public void withdraw(Long amount) {
        if (balance < amount) {
            throw new IllegalArgumentException();
        }
        this.balance -= amount;
    }
}


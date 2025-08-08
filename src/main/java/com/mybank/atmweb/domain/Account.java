package com.mybank.atmweb.domain;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Builder
@Getter
@Entity @Transactional
@AllArgsConstructor
public class Account {

    protected Account() {
    }
    @Id @GeneratedValue
    private Long id;

    private String accountNumber;
    private String accountName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    private String password;

    private Long balance;

    @CreationTimestamp
    private LocalDateTime createAt;

    @Enumerated(EnumType.STRING)
    @Column(name="bank")
    private BankType bank;
}

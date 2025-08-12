package com.mybank.atmweb.domain;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private String memo;

    private Long amount;

    @Builder
    public Transaction(Account account, TransferType transfer, Long amount,
                       Long balanceBefore, Long balanceAfter, String memo) {
        this.account = account;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.transfer = transfer;
        this.memo = memo;
    }

    private Long balanceBefore;
    private Long balanceAfter;

    @Enumerated(EnumType.STRING)
    private TransferType transfer;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void setMemo(String memo) {
        this.memo = memo;
    }
}

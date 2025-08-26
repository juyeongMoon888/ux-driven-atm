package com.mybank.atmweb.domain.transaction;

import com.mybank.atmweb.domain.BankType;
import com.mybank.atmweb.domain.TransferType;
import com.mybank.atmweb.domain.account.Account;
import com.mybank.atmweb.dto.TransactionStatus;
import com.mybank.atmweb.service.transfer.model.OperationType;
import jakarta.persistence.*;
import lombok.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name="from_bank")
    private BankType fromBank;

    @Column(name = "to_bank")
    private String toBank;

    @Builder
    public Transaction(Account account, OperationType operationType, Long amount,
                       Long balanceBefore, Long balanceAfter, String memo, BankType fromBank,
                       String toBank, String fromAccountNumber, String toAccountNumber,
                       TransactionStatus transactionStatus) {
        this.account = account;
        this.operationType = operationType;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.memo = memo;
        this.fromBank = fromBank;
        this.toBank = toBank;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.transactionStatus = transactionStatus;
    }

    private Long balanceBefore;
    private Long balanceAfter;

    private String fromAccountNumber;
    private String toAccountNumber;


    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20)
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false, length = 20)
    private TransactionStatus transactionStatus;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void setMemo(String memo) {
        this.memo = memo;
    }
}

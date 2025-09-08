package com.mybank.atmweb.domain;

import com.mybank.atmweb.dto.TransactionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@Transactional
@Getter @Setter
public class Idempotency {
    @Id
    @Column(name="idempotency_key")
    private String key; // 클라이언트가 준 UUID

    @Column(name = "tx_id")
    private Long txId; // 매핑된 거래 ID
    private LocalDateTime createdAt; //finalizedAt
    private TransactionStatus transactionStatus;
    private String failureCode;

    // 플레이스홀더 생성자: 등록시 기본값
    public Idempotency(String key) {
        this.key = key;
        this.transactionStatus = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.failureCode = null;
    }
}


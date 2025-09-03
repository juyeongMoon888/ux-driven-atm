package com.mybank.atmweb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExAccWithdrawRes {
    private boolean approved;   // 출금 승인 여부
    private String code;        // 결과 코드 (예: EX_WITHDRAW_OK, EX_WITHDRAW_FAIL)
    private String message;     // 결과 메시지
    private long exTxId;        // 외부은행 트랜잭션 ID
}

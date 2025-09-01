package com.mybank.atmweb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExAccDepositRes {
    private boolean success; // 입금 성공 여부
    private String code;
    private long exTxId; // withdraw 단계에서 받은 exTxId
}

package com.mybank.atmweb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExAccDepositRes {
    private boolean success; // 입금 성공 여부
    private String code;
    private String message;
    private String exTxId; // withdraw 단계에서 받은 exTxId

    public static ExAccDepositRes fail(String code, String message) {
        ExAccDepositRes res = new ExAccDepositRes();
        res.setSuccess(false);
        res.setCode(code);
        res.setMessage(message);
        return res;
    }
}

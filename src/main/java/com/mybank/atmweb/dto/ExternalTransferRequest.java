package com.mybank.atmweb.dto;

import com.mybank.atmweb.domain.BankType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter @Setter
public class ExternalTransferRequest {
    private BankType bankCode;
    private String accountNumber;
    private Long amount;
}

package com.mybank.atmweb.global.code;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

public enum PendingCode implements BaseCode {
    PENDING_CONFIRM("pending.external.confirm", HttpStatus.ACCEPTED)
    ;

    private final String messageKey;
    private final HttpStatus httpStatus;

    PendingCode(String messageKey, HttpStatus httpStatus) {
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

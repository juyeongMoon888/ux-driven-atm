package com.mybank.atmweb.global.code;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

public enum ErrorCode implements BaseCode{
    INVALID_CREDENTIALS("error.invalid_credentials", HttpStatus.UNAUTHORIZED),
    USER_EXIST("error.user_exist", HttpStatus.CONFLICT),
    SERVER_ERROR("error.server_error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND("error.user_not_found", HttpStatus.UNAUTHORIZED),
    DATA_INTEGRITY_VIOLATION("error.data_integrity_violation", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("error.validation_failed", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("error.token_expired", HttpStatus.UNAUTHORIZED),
    TOKEN_MALFORMED("error.token_malformed", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("error.token_invalid", HttpStatus.UNAUTHORIZED),
    AUTH_HEADER_INVALID("error.auth_header_invalid", HttpStatus.UNAUTHORIZED),
    AUTH_HEADER_MALFORMED("error.auth_header_malformed", HttpStatus.UNAUTHORIZED),
    TOKEN_BLACKLISTED("error.token_blacklisted", HttpStatus.UNAUTHORIZED),
    TOKEN_LOGGED_OUT("error.token_logged_out", HttpStatus.UNAUTHORIZED),
    BANK_INVALID("error.bank_invalid", HttpStatus.BAD_REQUEST),
    TOKEN_NOT_FOUND("error.token_not_found", HttpStatus.UNAUTHORIZED)
    ;

    private final String messageKey;
    private final HttpStatus httpStatus;

    ErrorCode(String messageKey, HttpStatus httpStatus) {
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

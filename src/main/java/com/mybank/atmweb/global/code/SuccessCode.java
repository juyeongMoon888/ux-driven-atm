package com.mybank.atmweb.global.code;

import org.springframework.http.HttpStatus;

public enum SuccessCode implements BaseCode{
    SIGNUP_SUCCESS("success.signup", HttpStatus.OK),
    ACCESS_TOKEN_REISSUED("success.access_token_reissued", HttpStatus.OK),
    LOGOUT_SUCCESS("success.logout", HttpStatus.OK)
    ;

    private final String messageKey;
    private final HttpStatus httpStatus;

    SuccessCode(String messageKey, HttpStatus httpStatus) {
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

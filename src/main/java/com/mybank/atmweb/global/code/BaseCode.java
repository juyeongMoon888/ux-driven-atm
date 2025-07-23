package com.mybank.atmweb.global.code;

import org.springframework.http.HttpStatus;

public interface BaseCode {
    String getMessageKey();
    HttpStatus getHttpStatus();
}

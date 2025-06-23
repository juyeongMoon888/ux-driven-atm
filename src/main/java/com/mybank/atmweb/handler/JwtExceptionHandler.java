package com.mybank.atmweb.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class JwtExceptionHandler {

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handlerExpiredToken(ExpiredJwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 만료되었습니다.");
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<String> handleInvalidSignature(io.jsonwebtoken.security.SignatureException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("잘못된 서명입니다.");
    }
}

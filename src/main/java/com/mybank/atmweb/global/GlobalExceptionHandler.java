package com.mybank.atmweb.global;

import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageUtil messageUtil;
    private final ResponseUtil responseUtil;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validationExceptionHandler(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();

            if (!errors.containsKey(field)) {
                String validationType = error.getCode();
                String code = (validationType + "_" +field).toUpperCase();
                errors.put(field, code);
            }
        });
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        return responseUtil.buildResponse(errorCode, errorCode.getHttpStatus(), errors);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> customExceptionHandler(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return responseUtil.buildResponse(errorCode, errorCode.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        ErrorCode errorCode = ErrorCode.SERVER_ERROR;
        return responseUtil.buildResponse(errorCode, errorCode.getHttpStatus());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        ErrorCode errorCode = ErrorCode.DATA_INTEGRITY_VIOLATION;
        return responseUtil.buildResponse(errorCode, errorCode.getHttpStatus());
    }

}

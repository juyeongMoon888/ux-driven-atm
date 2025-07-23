package com.mybank.atmweb.global;

import com.mybank.atmweb.dto.ApiResponse;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ResponseUtil {

    private final MessageUtil messageUtil;

    public ResponseUtil(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    public <T> ResponseEntity<ApiResponse<T>> buildResponse(Enum<?> codeEnum, HttpStatus status, T data) {
        String messageKey = null;

        if (codeEnum instanceof ErrorCode ec) {
            messageKey = ec.getMessageKey();
        } else if (codeEnum instanceof SuccessCode sc) {
            messageKey = sc.getMessageKey();
        }

        String code = codeEnum.name();
        String message = messageUtil.getMessage(messageKey);

        ApiResponse<T> body = new ApiResponse<>(code, message, data);
        return ResponseEntity.status(status).body(body);
    }

    public ResponseEntity<ApiResponse<Void>> buildResponse(Enum<?> codeEnum, HttpStatus status) {
        return buildResponse(codeEnum, status, null);
    }
}

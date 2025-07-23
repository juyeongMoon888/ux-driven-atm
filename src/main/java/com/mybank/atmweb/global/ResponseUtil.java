package com.mybank.atmweb.global;

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

    public ResponseEntity<?> buildResponse(Enum<?> codeEnum, HttpStatus status, Map<String, Object> data) {
        String messageKey = null;
        if (codeEnum instanceof ErrorCode ec) {
            messageKey = ec.getMessageKey();
        } else if (codeEnum instanceof SuccessCode sc) {
            messageKey = sc.getMessageKey();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", codeEnum.name());
        body.put("message", messageUtil.getMessage(messageKey));
        if (data != null && !data.isEmpty()) {
            body.put("data", data);
        }
        return ResponseEntity.status(status).body(body);
    }
}

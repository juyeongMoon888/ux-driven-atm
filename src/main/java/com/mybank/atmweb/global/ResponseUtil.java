package com.mybank.atmweb.global;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybank.atmweb.dto.ApiResponse;
import com.mybank.atmweb.global.code.BaseCode;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.SuccessCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class ResponseUtil {

    private final MessageUtil messageUtil;

    public ResponseUtil(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    public <T> ResponseEntity<ApiResponse<T>> buildResponse(BaseCode codeEnum, HttpStatus status, T data) {

        String code = ((Enum<?>)codeEnum).name();
        String message = messageUtil.getMessage(codeEnum.getMessageKey());

        ApiResponse<T> body = new ApiResponse<>(code, message, data);
        return ResponseEntity.status(status).body(body);
    }

    public ResponseEntity<ApiResponse<Void>> buildResponse(BaseCode codeEnum, HttpStatus status) {
        return buildResponse(codeEnum, status, null);
    }

    public HttpServletResponse writeHttpErrorResponse(HttpServletResponse response, BaseCode codeEnum) {
        response.setStatus(codeEnum.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        String code = ((Enum<?>)codeEnum).name();
        String message = messageUtil.getMessage(codeEnum.getMessageKey());

        String json = String.format("{\"code\":\"%s\",\"message\":\"%s\"}", code, message);
        try {
            response.getWriter().write(json);
            response.flushBuffer(); //더이상 흐르지 않게 한다. 버퍼 비우고 응답 완료
        } catch (IOException e) {
            log.error("응답 작성 중 IO 오류 발생: ", e);
        }
        return response;
    }

    private void fallbackSend(HttpServletResponse response) {
        try {
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("서버 오류가 발생했습니다.");
            response.flushBuffer();
        } catch (IOException e) {
            log.error("Fallback 응답도 실패", e);
        }
    }
}

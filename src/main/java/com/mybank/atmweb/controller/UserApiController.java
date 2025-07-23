package com.mybank.atmweb.controller;

import com.mybank.atmweb.dto.SignupForm;
import com.mybank.atmweb.global.MessageUtil;
import com.mybank.atmweb.global.ResponseUtil;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.security.CustomUserDetails;
import com.mybank.atmweb.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserApiController {

    private final UserService userService;
    private final ResponseUtil responseUtil;

    //회원가입 아이디 중복확인
    @GetMapping("check-id")
    public ResponseEntity<?> checkDuplicateLoginId(@Valid @RequestParam String loginId) {
        boolean isDuplicate = userService.isDuplicatedLoginId(loginId);
        return ResponseEntity.ok(Map.of("duplicate", isDuplicate));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupForm form) {
        userService.signup(form);
        return responseUtil.buildResponse(SuccessCode.SIGNUP_SUCCESS, HttpStatus.OK, null);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(Map.of("name", user.getUsername()));
    }
}

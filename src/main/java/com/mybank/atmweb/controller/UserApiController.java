package com.mybank.atmweb.controller;

import com.mybank.atmweb.dto.SignupForm;
import com.mybank.atmweb.security.CustomUserDetails;
import com.mybank.atmweb.auth.JwtUtil;
import com.mybank.atmweb.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    //회원가입 아이디 중복확인
    @GetMapping("check-id")
    public ResponseEntity<?> checkDuplicateLoginId(@Valid @RequestParam String loginId) {
        boolean isDuplicate = userService.isDuplicatedLoginId(loginId);
        return ResponseEntity.ok(Map.of("duplicate", isDuplicate));
    }

    //회원가입 완료 후
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupForm form) {
        userService.signup(form);
        return ResponseEntity.ok(Map.of(
                "code", "SIGNUP_SUCCESS",
                "message", "회원가입이 완료되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(Map.of("name", user.getUsername()));
    }
}

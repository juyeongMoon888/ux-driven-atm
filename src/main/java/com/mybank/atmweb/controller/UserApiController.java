package com.mybank.atmweb.controller;

import com.mybank.atmweb.dto.SignupForm;
import com.mybank.atmweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private UserService userService;

    //회원가입 아이디 중복확인
    @GetMapping("check-id")
    public ResponseEntity<?> checkId(@RequestParam String loginId) {
        if (loginId == null || loginId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "loginId는 필수입니다."));
        }
        boolean isAvailable = userService.isLoginIdAvailable(loginId);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }

    //회원가입 완료 후
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupForm form) {
        userService.signup(form);
        return ResponseEntity.ok().build();
    }
}

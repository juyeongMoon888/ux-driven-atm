package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.SignupForm;
import com.mybank.atmweb.security.CustomUserDetails;
import com.mybank.atmweb.security.JwtUtil;
import com.mybank.atmweb.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
        return ResponseEntity
                .ok(Map.of("message", "회원가입이 완료되었습니다."));
    }

    //사용자 정보
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String name = userDetails.getUsername();
        return ResponseEntity.ok(Map.of("name", name));
    }
}

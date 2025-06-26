package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.LoginRequest;
import com.mybank.atmweb.repository.UserRepository;
import com.mybank.atmweb.security.JwtUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthApiController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 1. 아이디로 사용자 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))  {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        //3. JWT 토큰 발급
        String token = jwtUtil.createToken(user.getId(), user.getRole().name());

        //4. 토큰을 JSON 바디로 응답
        return ResponseEntity.ok(Map.of("token", token));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); //세션 제거
        return ResponseEntity.ok("로그아웃 완료");
    }

}


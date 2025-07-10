package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.LoginRequest;
import com.mybank.atmweb.exception.user.UserNotFoundException;
import com.mybank.atmweb.repository.UserRepository;
import com.mybank.atmweb.auth.JwtUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // 1. 아이디로 사용자 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new UserNotFoundException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 2. 비밀번호 검증
        String rawPassword = request.getPassword();
        if (!passwordEncoder.matches(request.getPassword(), rawPassword))  {
            throw new UserNotFoundException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        //3. 로그인 성공 시
        String token = jwtUtil.createToken(user.getId(), user.getLoginId(), user.getRole().name());

        //4. 토큰을 JSON 바디로 응답
        return ResponseEntity.ok(Map.of("token", token));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); //세션 제거
        return ResponseEntity.ok("로그아웃 완료");
    }

}


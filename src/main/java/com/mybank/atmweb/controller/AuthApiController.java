package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.LoginRequest;
import com.mybank.atmweb.dto.LoginResponse;
import com.mybank.atmweb.exception.user.UserNotFoundException;
import com.mybank.atmweb.repository.UserRepository;
import com.mybank.atmweb.auth.JwtUtil;
import com.mybank.atmweb.service.AuthService;
import com.mybank.atmweb.service.TokenBlacklistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
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
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthService authService;
    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        // 1. 아이디로 사용자 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new UserNotFoundException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 2. 비밀번호 검증
        String rawPassword = request.getPassword();
        if (!passwordEncoder.matches(rawPassword, user.getPassword()))  {
            throw new UserNotFoundException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        //3. 로그인 성공 시
        LoginResponse accessToken = authService.login(user, response);

        //4. 토큰을 JSON 바디로 응답
        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken.getAccessToken(),
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getName()
                )));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = authService.findRefreshTokenFromCookies(cookies);

        if (!jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("code", "REFRESH_INVALID", "message", "Refresh Token이 유효하지 않습니다."));
        }

        Long userId = jwtUtil.getUserId(refreshToken);

        String savedToken = redisTemplate.opsForValue().get("refreshToken:" + userId);
        if (savedToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "code", "REFRESH_TOKEN_NOT_FOUND",
                    "message", "로그아웃되었거나 토큰이 저장되지 않았습니다."
            ));
        }
        if (!refreshToken.equals(savedToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "code", "INVALID_REFRESH_TOKEN",
                    "message", "위조된 토큰입니다."
            ));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtUtil.createAccessToken(user);

        return ResponseEntity.ok(Map.of(
                "code", "ACCESS_TOKEN_REISSUED",
                "message", "Access token이 재발급되었습니다.",
                "data", Map.of(
                        "accessToken", newAccessToken
                )
        ));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtUtil.extractToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            tokenBlacklistService.blacklistToken(token);
            Long userId = jwtUtil.getUserId(token);
            authService.logout(token, userId);
        }
        return ResponseEntity.ok(Map.of("code", "LOGOUT_SUCCESS", "message", "로그아웃 완료"));
    }

}


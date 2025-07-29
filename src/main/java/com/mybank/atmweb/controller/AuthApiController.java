package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.LoginRequest;
import com.mybank.atmweb.dto.LoginResponse;
import com.mybank.atmweb.global.MessageUtil;
import com.mybank.atmweb.global.ResponseUtil;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.code.SuccessCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.UserRepository;
import com.mybank.atmweb.auth.JwtUtil;
import com.mybank.atmweb.service.AuthService;
import com.mybank.atmweb.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.mybank.atmweb.service.AuthService.REFRESH_TOKEN_PREFIX;

@Slf4j
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
    private final MessageUtil messageUtil;
    private final ResponseUtil responseUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        // 1. 아이디로 사용자 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        // 2. 비밀번호 검증
        String rawPassword = request.getPassword();
        if (!passwordEncoder.matches(rawPassword, user.getPassword()))  {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
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

        try {
            jwtUtil.validateToken(refreshToken);
        } catch (ExpiredJwtException e) {
            //토큰 만료된 경우
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (MalformedJwtException e) {
            //토큰 구조가 잘못된 경우
            throw new CustomException(ErrorCode.TOKEN_MALFORMED);
        } catch (JwtException e) {
            // 그 외 위조된 경우 등
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        Long userId = jwtUtil.getUserId(refreshToken);

        String savedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (savedToken == null || !refreshToken.equals(savedToken)) {
            return responseUtil.buildResponse(ErrorCode.TOKEN_INVALID, HttpStatus.UNAUTHORIZED, null);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtUtil.createAccessToken(user);

        return ResponseEntity.ok(Map.of(
                "code", SuccessCode.ACCESS_TOKEN_REISSUED.name(),
                "message", messageUtil.getMessage(SuccessCode.ACCESS_TOKEN_REISSUED.getMessageKey()),
                "data", Map.of(
                        "accessToken", newAccessToken
                )
        ));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtUtil.extractToken(request);

        if (token != null) {
            try {
                jwtUtil.validateToken(token);
                Long ttl = jwtUtil.getExpirationMillis(token);
                tokenBlacklistService.blacklistToken(token, ttl);
                Long userId = jwtUtil.getUserId(token);
                authService.logout(token, userId);
            } catch (JwtException e) {
                log.warn("유효하지 않은 JWT: {}", e.getMessage());
            }
        }
        return responseUtil.buildResponse(SuccessCode.LOGOUT_SUCCESS, HttpStatus.OK, null);
    }

}


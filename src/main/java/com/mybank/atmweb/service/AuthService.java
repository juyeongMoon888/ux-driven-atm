package com.mybank.atmweb.service;

import com.mybank.atmweb.auth.JwtUtil;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.LoginResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    public static final String ACCESS_TOKEN_PREFIX = "accessToken:";
    public static final String REFRESH_TOKEN_PREFIX = "refreshToken:";

    public final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    public LoginResponse login(User user, HttpServletResponse response) {
        //1. 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        //2. 만료 시간 계산 (JWT exp 기반)
        long now = System.currentTimeMillis();
        long accessTokenTtl = jwtUtil.getExpirationMillis(accessToken);
        long refreshTokenTtl = jwtUtil.getExpirationMillis(refreshToken);

        //3. Redis 저장 (JWT TTL 동기화)
        redisTemplate.opsForValue().set(
                ACCESS_TOKEN_PREFIX + user.getId(),
                accessToken,
                Duration.ofMillis(accessTokenTtl)
        );

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                Duration.ofMillis(refreshTokenTtl)
        );

        // 4. RefreshToken을 HttpOnly Cookie로 전송
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) //로컬 한정
                .path("/")
                .maxAge(Duration.ofMillis(refreshTokenTtl))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return new LoginResponse(accessToken);
    }

    public void logout(String accessToken, Long userId) {
        redisTemplate.delete(ACCESS_TOKEN_PREFIX + userId);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    public String findRefreshTokenFromCookies(Cookie[] cookies) {
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}

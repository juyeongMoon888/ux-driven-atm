package com.mybank.atmweb.service;

import com.mybank.atmweb.auth.JwtUtil;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    public final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    public LoginResponse login(User user, HttpServletResponse response) {
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        redisTemplate.opsForValue().set(
                "accessToken:" + user.getId(),
                accessToken,
                Duration.ofDays(15)
        );

        redisTemplate.opsForValue().set(
                "refreshToken:" + user.getId(),
                refreshToken,
                Duration.ofDays(7)
        );

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return new LoginResponse(accessToken);
    }

    public void logout(String accessToken, Long userId) {
        long expiration = jwtUtil.getExpirationMillis(accessToken);

        redisTemplate.opsForValue().set(
                "blacklist:" + accessToken,
                "true",
                Duration.ofMillis(expiration)
        );
        redisTemplate.delete("accessToken:" + userId);
        redisTemplate.delete("refreshToken:" + userId);
    }
}

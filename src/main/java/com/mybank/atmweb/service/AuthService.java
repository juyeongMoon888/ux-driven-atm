package com.mybank.atmweb.service;

import com.mybank.atmweb.auth.JwtUtil;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    public final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    public LoginResponse login(User user) {
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        //redis에 토큰 저장
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

        return new LoginResponse(accessToken, refreshToken);
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

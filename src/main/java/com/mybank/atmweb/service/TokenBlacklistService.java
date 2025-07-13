package com.mybank.atmweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;

    public void blacklistToken(String token) {
        redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofHours(1));
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}

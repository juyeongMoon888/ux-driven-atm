package com.mybank.atmweb.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisCleaner implements ApplicationRunner {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Set<String> accessTokenKeys = redisTemplate.keys("accessToken:*");
        Set<String> refreshTokenKeys = redisTemplate.keys("refreshToken:*");
        if (accessTokenKeys != null && !accessTokenKeys.isEmpty()) {
            redisTemplate.delete(accessTokenKeys);
        }
        if (refreshTokenKeys != null && !refreshTokenKeys.isEmpty()) {
            redisTemplate.delete(refreshTokenKeys);
        }
    }
}


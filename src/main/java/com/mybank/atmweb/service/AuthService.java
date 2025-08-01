package com.mybank.atmweb.service;

import com.mybank.atmweb.auth.JwtUtil;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.TokenDto;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    public static final String ACCESS_TOKEN_PREFIX = "accessToken:";
    public static final String REFRESH_TOKEN_PREFIX = "refreshToken:";

    public final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenDto issueTokens(User user) {
       TokenDto tokens = TokenDto.createFromUser(user, jwtUtil);

       redisTemplate.opsForValue().set(
               ACCESS_TOKEN_PREFIX + user.getId(),
               tokens.getAccessToken(),
               Duration.ofMillis(tokens.getAccessTokenTtl())
       );

       redisTemplate.opsForValue().set(
               REFRESH_TOKEN_PREFIX + user.getId(),
               tokens.getRefreshToken(),
               Duration.ofMillis(tokens.getRefreshTokenTtl())
       );

        return tokens;
    }

    public User findUserByLoginIdOrThrow(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));
    }
    public User findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public void validatePassword(String rawPassword, String password) {
        if (!passwordEncoder.matches(rawPassword, password)) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    public void logout(Long userId) {
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

    public TokenDto refresh(User user) {
        TokenDto newTokens = TokenDto.createFromUser(user, jwtUtil);
        System.out.println("🔥재발급 완료="+newTokens.getAccessToken());
        redisTemplate.opsForValue().set(
                ACCESS_TOKEN_PREFIX + user.getId(),
                newTokens.getAccessToken(),
                Duration.ofMillis(newTokens.getAccessTokenTtl())
        );
        return newTokens;
    }
}

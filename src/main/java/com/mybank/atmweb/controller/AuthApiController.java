package com.mybank.atmweb.controller;

import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.LoginRequest;
import com.mybank.atmweb.dto.TokenDto;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

import static com.mybank.atmweb.service.AuthService.REFRESH_TOKEN_PREFIX;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthService authService;
    private final RedisTemplate<String, String> redisTemplate;
    private final MessageUtil messageUtil;
    private final ResponseUtil responseUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        // 1. 아이디로 사용자 조회
        User user = authService.findUserByLoginIdOrThrow(request.getLoginId());

        // 2. 비밀번호 검증
        authService.validatePassword(request.getPassword(), user.getPassword());

        //3. 토큰 발급 및 redis 저장
        TokenDto tokens = authService.issueTokens(user);

        //4. 토큰 쿠키로 저장
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokens.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(tokens.getAccessTokenTtl()))
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", accessTokenCookie.toString());

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(tokens.getRefreshTokenTtl()))
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        //5. 토큰을 JSON 바디로 응답
        return ResponseEntity.ok(Map.of(
                "accessToken", tokens.getAccessToken(),
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getName()
                )));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = authService.findRefreshTokenFromCookies(cookies);//@CookieValue로 생략될 부분

        //1. 쿠키에 refreshToken이 존재하지 않으면 재발급 불가
        if (refreshToken == null) {
            return responseUtil.buildResponse(ErrorCode.TOKEN_INVALID, HttpStatus.UNAUTHORIZED, null);
        }

        try {
            //2. 토큰 유효성 검증 (만료, 위조 등 체크)
            jwtUtil.validateToken(refreshToken);
        } catch (ExpiredJwtException e) {
            //토큰 만료된 경우
            System.out.println("ExpiredJwtException 발생");
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (MalformedJwtException e) {
            //토큰 구조가 잘못된 경우
            System.out.println("MalformedJwtException 발생");
            throw new CustomException(ErrorCode.TOKEN_MALFORMED);
        } catch (JwtException e) {
            // 그 외 위조된 경우 등
            System.out.println("JwtException 발생");
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        // 3. userId 추출
        Long userId = jwtUtil.getUserId(refreshToken);

        // 4. Redis 저장값과 비교
        String savedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (savedToken == null || !refreshToken.equals(savedToken)) {
            return responseUtil.buildResponse(ErrorCode.TOKEN_INVALID, HttpStatus.UNAUTHORIZED, null);
        }

        User user = authService.findUserByIdOrThrow(userId);

        //5. 재발급 토큰 생성 및 쿠키 저장
        TokenDto newTokens = authService.refresh(user);
        ResponseCookie newAccessToken = ResponseCookie.from("accessToken", newTokens.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(newTokens.getAccessTokenTtl()))
                .build();
        response.addHeader("Set-Cookie", newAccessToken.toString());

        return ResponseEntity.ok(Map.of(
                "code", SuccessCode.ACCESS_TOKEN_REISSUED.name(),
                "message", messageUtil.getMessage(SuccessCode.ACCESS_TOKEN_REISSUED.getMessageKey()),
                "data", Map.of(
                        "accessToken", newTokens.getAccessToken()
                )
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtUtil.extractToken(request);
        if (token != null) {
            try {
                jwtUtil.validateToken(token);
                Long ttl = jwtUtil.getExpirationMillis(token);
                tokenBlacklistService.blacklistToken(token, ttl);
                Long userId = jwtUtil.getUserId(token);
                authService.logout(userId);

                ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", "")
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .maxAge(0)
                        .sameSite("Lax")
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

                ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .maxAge(0)
                        .sameSite("Lax")
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
            } catch (JwtException e) {
                log.warn("유효하지 않은 JWT: {}", e.getMessage());
            }
        }

        return responseUtil.buildResponse(SuccessCode.LOGOUT_SUCCESS, HttpStatus.OK, null);
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkLoginStatus(HttpServletRequest request){
        System.out.println("checkLoginStatus 진입");
        String token = jwtUtil.extractToken(request);
        if (token == null) {
            throw new CustomException(ErrorCode.TOKEN_NOT_FOUND);
        }
        try {
            //2. 토큰 유효성 검증 (만료, 위조 등 체크)
            jwtUtil.validateToken(token);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (ExpiredJwtException e) {
            //토큰 만료된 경우
            System.out.println("ExpiredJwtException 발생");
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (MalformedJwtException e) {
            //토큰 구조가 잘못된 경우
            System.out.println("MalformedJwtException 발생");
            throw new CustomException(ErrorCode.TOKEN_MALFORMED);
        } catch (JwtException e) {
            // 그 외 위조된 경우 등
            System.out.println("JwtException 발생");
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }
}


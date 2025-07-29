package com.mybank.atmweb.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.atmweb.global.ResponseUtil;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.security.CustomUserDetailsService;
import com.mybank.atmweb.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.mybank.atmweb.service.AuthService.ACCESS_TOKEN_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenBlacklistService tokenBlacklistService;
    private final ResponseUtil responseUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        System.out.println("jwt필터 진입");

        //JWT 검사 제외 대상
        List<String> whitelist = List.of(
                "/",
                "/login",
                "/signup",
                "/api/ping",
                "/api/users/signup",
                "/api/users/check-id",
                "/api/auth/login",
                "/actuator/**"
        );

        if (whitelist.contains(path) ||
                path.equals("/error") ||
                path.startsWith("/js") ||
                path.startsWith("/css") ||
                path.startsWith("/api/auth") ||
                path.startsWith("/images") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/actuator")
        ) {

            filterChain.doFilter(request, response);
            return;
        }


        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 1. Authorization 헤더 우선
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            responseUtil.writeHttpErrorResponse(response, ErrorCode.AUTH_HEADER_INVALID);
            return;
        }
        if (!authHeader.startsWith("Bearer ")) {
            responseUtil.writeHttpErrorResponse(response, ErrorCode.AUTH_HEADER_MALFORMED);
            return;
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 2. 여전히 token 없으면 인증 실패
        if (token == null || token.isBlank()) {
            responseUtil.writeHttpErrorResponse(response, ErrorCode.AUTH_HEADER_INVALID);
            return;
        }

        if (token != null) {

            try {
                String loginId = jwtUtil.getLoginIdFromToken(token);
                Long userId = jwtUtil.getUserId(token);

                if (tokenBlacklistService.isBlacklisted(token)) {
                    responseUtil.writeHttpErrorResponse(response, ErrorCode.TOKEN_BLACKLISTED);
                    return;
                }

                String redisToken = redisTemplate.opsForValue().get(ACCESS_TOKEN_PREFIX + userId);
                if (redisToken == null || !redisToken.equals(token)) {
                    responseUtil.writeHttpErrorResponse(response, ErrorCode.TOKEN_LOGGED_OUT);
                    return;
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException e) {
                responseUtil.writeHttpErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
                return;
            }

            catch (JwtException e) {
                responseUtil.writeHttpErrorResponse(response, ErrorCode.TOKEN_INVALID);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}



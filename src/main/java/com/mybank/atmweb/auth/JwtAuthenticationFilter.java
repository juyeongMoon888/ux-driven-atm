package com.mybank.atmweb.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.atmweb.security.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("🔍 Incoming path: " + request.getRequestURI());
        System.out.println("Method: " + request.getMethod());
        String path = request.getRequestURI();

        //JWT 검사 제외 대상
        List<String> whitelist = List.of(
                "/",
                "/login",
                "/signup",
                "/api/ping",
                "/api/users/signup",
                "/api/users/check-id",
                "/api/auth/login"
        );

        if (whitelist.contains(path) ||
                path.equals("/error") ||
                path.startsWith("/js") ||
                path.startsWith("/css") ||
                path.startsWith("/api/auth") ||
                path.startsWith("/images") ||
                path.startsWith("/favicon.ico")
        ) {

            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            setUnauthorizedResponse(response, "AUTH_HEADER_MISSING", "Authorization 헤더가 없거나 형식이 올바르지 않습니다.");
            return;
        }
        if (!authHeader.startsWith("Bearer ")) {
            setUnauthorizedResponse(response, "MALFORMED_AUTH_HEADER", "Authorization 헤더가 없거나 형식이 올바르지 않습니다.");
            return;
        }

        String token = authHeader.substring(7);


        try {
            String loginId = jwtUtil.getLoginIdFromToken(token);
            Long userId = jwtUtil.getUserId(token);

            String redisToken = redisTemplate.opsForValue().get("accessToken:" + userId);
            if (redisToken == null || !redisToken.equals(token)) {
                setUnauthorizedResponse(response,  "TOKEN_LOGGED_OUT", "만료되었거나 로그아웃된 토큰입니다.");
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ExpiredJwtException e) {
            setUnauthorizedResponse(response, "TOKEN_EXPIRED", "토큰이 만료되었습니다.");
            return;
        }

        catch (JwtException e) {
            setUnauthorizedResponse(response, "TOKEN_INVALID", "유효하지 않거나 만료된 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setUnauthorizedResponse(HttpServletResponse response, String errorCode, String message) {
        try {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, String> error = Map.of(
                    "code", errorCode,
                    "message", message
            );
            new ObjectMapper().writeValue(response.getWriter(), error);
            response.flushBuffer();
        } catch (JsonMappingException e) {
            log.error("setUnauthorizedResponse 중 JSON 매핑 예외 발생: ", e);
            fallbackSend(response);
        } catch (JsonProcessingException e) {
            log.error("setUnauthorizedResponse 중 JSON 매핑 예외 발생: ", e);
            fallbackSend(response);
        } catch (IOException e) {
            log.error("Fallback 처리도 실패: ", e);
        }
    }

    private void fallbackSend(HttpServletResponse response) {
        try {
            response.reset();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("code=UNAUTHORIZED\nmessage=인증 실패 (fallback)");
            response.flushBuffer();
        } catch (IOException e) {
            log.error("Fallback 응답도 실패", e);
        }
    }
}



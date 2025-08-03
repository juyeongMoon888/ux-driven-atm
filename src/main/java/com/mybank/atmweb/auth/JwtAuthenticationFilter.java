package com.mybank.atmweb.auth;

import com.mybank.atmweb.global.ResponseUtil;
import com.mybank.atmweb.global.code.ErrorCode;
import com.mybank.atmweb.global.exception.user.CustomException;
import com.mybank.atmweb.security.CustomUserDetailsService;
import com.mybank.atmweb.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
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
        System.out.println("request.getRequestURI() = " + request.getRequestURI());

        //JWT 검사 제외 대상
        List<String> whitelist = List.of(
                "/",
                "/login",
                "/signup",
                "/main",
                "/api/ping",
                "/api/users/signup",
                "/api/users/check-id",
                "/api/auth/login",
                "/api/auth/token/refresh",
                "/api/auth/logout",
                "/actuator/**"
        );
        if (whitelist.contains(path) ||
                path.equals("/error/") ||
                path.startsWith("/js/") ||
                path.startsWith("/css/") ||
                path.startsWith("/images/") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/actuator/")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // 1. 쿠키 검사
        token = jwtUtil.extractToken(request);
        System.out.println("🔥token = " + token);
        //3. token 유효성 검사
        if (token == null || token.isBlank()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        if (token == null || token.isBlank()) {
            log.warn("❌ [JwtFilter] token is null or blank → 인증 실패 처리 시작");
            request.setAttribute("tokenDebug", "token null or blank");
            responseUtil.writeHttpErrorResponse(response, ErrorCode.AUTH_HEADER_INVALID);
            return;
        }

        if (token != null) {

            System.out.println("token = " + token);
            try {
                String loginId = jwtUtil.getLoginIdFromToken(token);
                System.out.println("토큰 유지된다면 loginId="+ loginId);
                Long userId = jwtUtil.getUserId(token);
                System.out.println("토큰 유지된다면 userId="+ userId);


                if (tokenBlacklistService.isBlacklisted(token)) {
                    responseUtil.writeHttpErrorResponse(response, ErrorCode.TOKEN_BLACKLISTED);
                    return;
                }

                String redisToken = redisTemplate.opsForValue().get(ACCESS_TOKEN_PREFIX + userId);
                System.out.println("redisToken = " + redisToken);
                if (redisToken == null || !redisToken.equals(token)) {
                    System.out.println("!redisToken.equals(token)="+token);
                    responseUtil.writeHttpErrorResponse(response, ErrorCode.TOKEN_LOGGED_OUT);
                    return;
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
                System.out.println("userDetails = " + userDetails);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                System.out.println("authentication = " + authentication);
                jwtUtil.validateToken(token);
                token = jwtUtil.extractToken(request);
                request.setAttribute("accessTokenPresent", token != null); //여기까지 찍음
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException e) {
                System.out.println("ExpiredJwtException e 예외");
                responseUtil.writeHttpErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
                return;
            }

            catch (JwtException e) {
                System.out.println("JwtException e 예외");
                responseUtil.writeHttpErrorResponse(response, ErrorCode.TOKEN_INVALID);
                return;
            }
            catch (Exception e) {
                log.warn("🔴 [기타 JWT 오류] {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}



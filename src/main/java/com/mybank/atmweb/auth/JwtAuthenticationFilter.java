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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
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
                "/auth/refresh-redirect",
                "/api/auth/logout",
                "/api/auth/check",
                "/actuator/**"
        );
        if (whitelist.contains(path) ||
                path.startsWith("/js/") ||
                path.equals("/error/") ||
                path.startsWith("/css/") ||
                path.startsWith("/images/") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/actuator/")

        ) {
            System.out.println("✅ JS 요청이므로 필터 통과");
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // 1. 쿠키 검사
        token = jwtUtil.extractToken(request);
        log.info("🥠 token = {}", token);
        //3. token 유효성 검사
        if (token == null || token.isBlank()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        if (token == null || token.isBlank()) {
            log.warn("❌ [JwtFilter] token is null or blank → 인증 실패 처리 시작");
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        if (token != null) {
            try {
                //토큰 파싱
                String loginId = jwtUtil.getLoginIdFromToken(token);
                Long userId = jwtUtil.getUserId(token);

                //블랙리스트 차단 -> 인증 실패로 던짐 (로그아웃된 유저)
                if (tokenBlacklistService.isBlacklisted(token)) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                //로그아웃/토큰 미스매치 -> 인증 실패로 던짐
                String redisToken = redisTemplate.opsForValue().get(ACCESS_TOKEN_PREFIX + userId);
                if (redisToken == null || !redisToken.equals(token)) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                // 인증 성공 컨텍스트 설정
                UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (ExpiredJwtException e) {
                // 만료 → 인증 실패로 던짐 (EntryPoint가 처리)
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            } catch (JwtException e) {
                // 위조/서명오류 등 → 인증 실패로 던짐
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}



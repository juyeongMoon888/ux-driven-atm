package com.mybank.atmweb.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.atmweb.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
                path.startsWith("/api/auth") ||   // 전체 인증 관련 경로
                path.startsWith("/images") ||
                path.startsWith("/favicon.ico")
        ) {
            System.out.println("✅ JWT 필터 우회 경로 통과: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            SecurityContextHolder.clearContext();

            setUnauthorizedResponse(response, "Authorization 헤더가 없거나 형식이 올바르지 않습니다.");
            return;
        }


        String token = authHeader.substring(7);

        try {
            String loginId = jwtUtil.getLoginIdFromToken(token);
            Long userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);

            String redisToken = redisTemplate.opsForValue().get("accessToken:" + userId);
            if (redisToken == null || !redisToken.equals(token)) {
                SecurityContextHolder.clearContext();
                setUnauthorizedResponse(response,  "만료되었거나 로그아웃된 토큰입니다.");
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            setUnauthorizedResponse(response, "유효하지 않거나 만료된 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, String> error = Map.of(
                "code", "SESSION_EXPIRED",
                "message", message
        );
        new ObjectMapper().writeValue(response.getWriter(), error);
    }
}



package com.mybank.atmweb.config;

import com.mybank.atmweb.auth.JwtAuthenticationFilter;
import com.mybank.atmweb.auth.MyEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/signup",
                                "/main",
                                "/error",
                                "/api/ping",
                                "/api/users/signup",
                                "/api/users/check-id",
                                "/api/auth/login",
                                "/api/auth/token/refresh",
                                "/api/auth/logout",
                                "/api/auth/check",
                                "/auth/refresh-redirect"
                        )
                        .permitAll()
                        .requestMatchers("/js/**", "/css/**", "/favicon.ico").permitAll()
                        .requestMatchers(
                                "/api/users/me",
                                "/api/bank/**",
                                "/api/external-bank/**",
                                "/bank",
                                "/bank/**"
                        ).authenticated()
                        .anyRequest().denyAll()
                )
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new MyEntryPoint("/auth/refresh-redirect"),
                                r -> {
                                    String p = r.getServletPath();
                                    return p != null && (p.equals("/bank") || p.startsWith("/bank/"));
                                }
                        )
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> {
                                    String p = request.getServletPath();
                                    return p != null && p.startsWith("/api/");
                                }
                        )
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

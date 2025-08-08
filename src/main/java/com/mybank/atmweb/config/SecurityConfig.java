package com.mybank.atmweb.config;

import com.mybank.atmweb.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                                "/bank",
                                "/bank/open-account",
                                "/bank/accounts")
                        .permitAll()
                        .requestMatchers("/js/**", "/css/**", "/favicon.ico").permitAll()
                        .requestMatchers(
                                "/api/users/me",
                                "/api/bank/open-account",
                                "/api/bank/account-list"
                        ).authenticated()
                        .anyRequest().denyAll()
                )
                .formLogin(login -> login
                        .loginPage("/login")//로그인 안 되어 있으면 리디렉션
                        .permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

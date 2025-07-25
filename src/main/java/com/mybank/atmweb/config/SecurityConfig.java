package com.mybank.atmweb.config;

import com.mybank.atmweb.exception.CustomLoginFailureHandler;
import com.mybank.atmweb.auth.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Autowired
    private CustomLoginFailureHandler customLoginFailureHandler;
    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("스프링 시큐리티 진입");
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/signup",
                                "/error",
                                "/api/ping",
                                "/api/users/signup",
                                "/api/users/check-id",
                                "/api/auth/login")
                        .permitAll()
                        .requestMatchers("/js/**", "/css/**").permitAll()
                        .requestMatchers("/api/users/me").authenticated() //이거 없으면 403에러뜸// 내용 정리하기
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

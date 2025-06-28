package com.mybank.atmweb.config;

import com.mybank.atmweb.handler.CustomLoginFailureHandler;
import com.mybank.atmweb.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Autowired
    private CustomLoginFailureHandler customLoginFailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf->csrf.disable())

                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers(
                                "/",
                                "/login",
                                "/signup",
                                "/banking",
                                "/api/auth/**",
                                "/api/ping",
                                "/api/users/**",
                                "/css/**",
                                "/js/**")
                        .permitAll() //메인 페이지 접근 허용 (비로그인 허용)
                        .requestMatchers("/atm/**").authenticated() //로그인 필요
                        .anyRequest().denyAll()
                )
                .formLogin(login -> login
                        .loginPage("/login")//로그인 안 되어 있으면 리디렉션
                        .permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); //필터 등록
                return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

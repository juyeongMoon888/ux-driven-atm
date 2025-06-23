package com.mybank.atmweb.controller;

import com.mybank.atmweb.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class UserApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void shouldReturn401_whenTokenIsExpired() throws Exception {
        //given
        String expiredToken = Jwts.builder()
                .setSubject("1")
                .setExpiration(new Date(System.currentTimeMillis() - 10000)) //이미 만료
                .signWith(Keys.hmacShaKeyFor("your-256-bit-secret-your-256-bit-secret".getBytes()))
                .compact();

        //when & then
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}

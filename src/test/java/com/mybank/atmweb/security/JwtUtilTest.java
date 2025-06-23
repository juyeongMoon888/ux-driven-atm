package com.mybank.atmweb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.Date;

public class JwtUtilTest {
    private JwtUtil jwtUtil;
    private Key secretKey;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();

        Field field = JwtUtil.class.getDeclaredField("SECRET_KEY");
        field.setAccessible(true);
        secretKey = (Key) field.get(null);
    }

    @Test
    void shouldParseValidTokenSuccessfully() {
        //given
        Long userId = 1L;
        String role = "USER";

        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(new Date()) //지금 발급
                .setExpiration(new Date(System.currentTimeMillis() + 10000)) //10초 유효
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        //when
        Claims claims = jwtUtil.parseToken(token);

        //then
        Assertions.assertEquals("1", claims.getSubject());
        Assertions.assertEquals("USER", claims.get("role"));
    }
    @Test
    void shouldThrowExpiredJwtException_whenTokenIsExpired() {
        //given
        String expiredToken = Jwts.builder()
                .setSubject("1")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000)) //10초 전 발급
                .setExpiration(new Date(System.currentTimeMillis() - 5000)) //5초 전에 만료
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        //when & then
        Assertions.assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.parseToken(expiredToken);
        });
    }
    @Test
    void shouldThrowSignatureException_whenTokenSignatureIsInvalid() {
        //given
        Key anotherKey = Keys.hmacShaKeyFor("different-secret-key-different-secret".getBytes());
        String forgedToken = Jwts.builder()
                .setSubject("1")
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(anotherKey, SignatureAlgorithm.HS256) //다른 키로 서명
                .compact();

        //when & then
        Assertions.assertThrows(SignatureException.class, () -> {
            jwtUtil.parseToken(forgedToken);
        });
    }
}

package com.mybank.atmweb.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET ="your-256-bit-secret-your-256-bit-secret";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes()); //최소 32바이트 이상
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;//1시간

    //	로그인 성공 시 JWT 생성
    public String createToken(Long id, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    //	필터에서 들어온 요청의 JWT 해석
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}

package com.mybank.atmweb.dto;

import com.mybank.atmweb.auth.JwtUtil;
import com.mybank.atmweb.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenDto {

    private final String accessToken;
    private final String refreshToken;
    private final Long accessTokenTtl;
    private final Long refreshTokenTtl;

    public static TokenDto createFromUser(User user, JwtUtil jwtUtil) {
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);
        Long accessTokenTtl = jwtUtil.getExpirationMillis(accessToken);
        Long refreshTokenTtl = jwtUtil.getExpirationMillis(refreshToken);
        return new TokenDto(accessToken, refreshToken, accessTokenTtl, refreshTokenTtl);
    }
}

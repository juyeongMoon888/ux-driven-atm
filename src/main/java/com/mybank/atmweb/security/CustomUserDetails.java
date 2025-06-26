package com.mybank.atmweb.security;

import com.mybank.atmweb.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    public Long getId() {
        return user.getId();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return String.valueOf(user.getId());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; //계정 만료 안 됨
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; //잠금 아님
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; //비밀번호 만료 안 됨
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성 상태
    }
}

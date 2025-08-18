package com.mybank.atmweb.service;

import com.mybank.atmweb.dto.UserProfileDto;

import java.util.Optional;

public interface ProfileService {
    UserProfileDto getProfile(Long userId); //없으면 예외
    Optional<UserProfileDto> findProfile(Long userId); //선택적 조회
}

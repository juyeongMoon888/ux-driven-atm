package com.mybank.atmweb.service;

import com.mybank.atmweb.domain.UserProfile;
import com.mybank.atmweb.dto.UserProfileDto;
import com.mybank.atmweb.global.exception.user.ProfileNotFoundException;
import com.mybank.atmweb.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DefaultProfileService implements ProfileService{
    private final UserProfileRepository repository;

    @Override
    public UserProfileDto getProfile(Long userId) {
        return repository.findByUserId(userId)
                .map(this::toView)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
    }

    @Override
    public Optional<UserProfileDto> findProfile(Long userId) {
        return repository.findByUserId(userId).map(this::toView);
    }

    private UserProfileDto toView(UserProfile e) {
        return new UserProfileDto(e.getUser().getId(), e.getName(), e.getBirth(), e.getPhone());
    }
}

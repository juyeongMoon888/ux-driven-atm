package com.mybank.atmweb.assembler;

import com.mybank.atmweb.dto.AccountOpenRequestDto;
import com.mybank.atmweb.dto.ExternalOpenAccountRequestDto;
import com.mybank.atmweb.dto.UserProfileDto;
import com.mybank.atmweb.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalAccountRequestAssembler {

    private final ProfileService profileService;

    public ExternalOpenAccountRequestDto toExternalRequest(AccountOpenRequestDto dto, Long userId) {
        UserProfileDto p = profileService.getProfile(userId);
        return ExternalOpenAccountRequestDto.builder()
                .bank(dto.getBank())
                .accountName(dto.getAccountName())
                .userId(p.getUserId())
                .name(p.getName())
                .birth(p.getBirth())
                .phone(p.getPhone())
                .build();
    }
}

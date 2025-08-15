package com.mybank.atmweb.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserProfile {
    private final Long userId;
    private final String name;
    private final String birth; // "yyyy-MM-dd"
    private final String phone;
}

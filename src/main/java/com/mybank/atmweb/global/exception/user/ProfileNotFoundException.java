package com.mybank.atmweb.global.exception.user;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(Long userId) {
        super("Profile not found for userId=" + userId);
    }
}

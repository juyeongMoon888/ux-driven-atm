package com.mybank.atmweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignupForm {
    @NotBlank
    private String name;

    @NotBlank
    @Size(min = 6, max = 6)
    private String residentNumber;

    private String gender;
    private String phoneNumber;

    @NotBlank
    private String loginId;
    @NotBlank
    private String password;
}

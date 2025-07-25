package com.mybank.atmweb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder
public class SignupForm {
    @NotBlank(message = "{validation.login_id.required}")
    private String loginId;

    @NotBlank(message="{validation.password.required}")
    @Size(min = 8, message = "{validation.password.size}")
    private String password;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    private String email;

    @NotBlank(message="{validation.name.required}")
    private String name;

    @NotBlank(message="{validation.resident_number.required}")
    @Size(min = 13, max = 13, message = "{validation.resident_number.size}")
    private String residentNumber;

    @NotBlank(message="{validation.gender.required}")
    private String gender;

    @NotBlank(message="{validation.phone_number.required}")
    @Pattern(regexp = "^\\d{10,11}$", message = "{validation.phone_number.pattern}")
    private String phoneNumber;
}

package com.mybank.atmweb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignupForm {
    @NotBlank(message="아이디는 필수입니다.")
    private String loginId;
    @NotBlank(message="비밀번호는 필수입니다.")
    private String password;
    @NotBlank(message="이름은 필수입니다.")
    private String name;
    @NotBlank(message="주민등록번호는 필수입니다.")
    @Size(min = 13, max = 13)
    private String residentNumber;
    @NotBlank(message="성별은 필수입니다.")
    private String gender;
    @NotBlank(message="전화번호는 필수입니다.")
    private String phoneNumber;
}

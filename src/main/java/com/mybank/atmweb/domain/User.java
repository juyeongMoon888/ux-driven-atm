package com.mybank.atmweb.domain;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter @Setter
@Entity @Transactional
@Table(name = "member")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String cardPw;
    private String gender;
    private String phoneNumber;
    private String residentNumber;

    @Column(unique=true)
    private String loginId; // 로그인에 사용하는 ID (username 역할)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

}

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

    /**
     * 로그인 실패 횟수를 저장하는 필드입니다.
     * DB 컬럼이 NOT NULL 제약을 갖고 있어, 명시적으로 기본값 0을 설정해줍니다.
     * 해당 설정이 없으면 회원가입 시 null 저장으로 인해 DataIntegrityViolationException이 발생할 수 있습니다.
     */
    @Column(nullable = false)
    private int retryCount = 0;
}

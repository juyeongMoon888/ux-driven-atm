package com.mybank.atmweb.domain;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity @Transactional
@Table(name = "member")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    
    private String name; //이름
    private String cardPw; //카드 비밀번호 (암호화 할것)
    private String gender; //성별
    private String phoneNumber; //전화번호
    private int retryCount; // 로그인 시도 횟수
    private String loginId; //로그인 ID
    private String password;//로그인 PW
    private String residentNumber; //주민등록번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL) //jakarta.persistence 가 표준임
    private Account account;

}

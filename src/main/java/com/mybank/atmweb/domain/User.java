package com.mybank.atmweb.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
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

    //주인X 왜래키는 Account에 있으므로 Account가 연관관계의 주인이다.//관계를 실제로 저장,변경하는 쪽//User은 Account를 모른다.
    //User는 매핑된 거울일 뿐이다. //읽기 전용 //DB 반영 가능 불가
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL) //jakarta.persistence 가 표준임
    private Account account;

    //생성자, getter/setter, 비밀번호 체크 등
}

package com.mybank.atmweb.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @Column(name="user_id")
    private Long userId; // PK = users.id

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false, updatable = false)
    private User user; //user.id와 동일(공유 PK)

    private String name;
    private String birth;
    private String phone;
}

package com.streaming.settlement.system.memberservice.domain.core.entity;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.OAuthProvider;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Role;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Tier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private Long email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Tier tier;

    private OAuthProvider oAuthProvider;

}

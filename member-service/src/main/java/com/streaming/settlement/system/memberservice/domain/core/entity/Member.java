package com.streaming.settlement.system.memberservice.domain.core.entity;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.OAuthProvider;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Role;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Tier;
import com.streaming.settlement.system.memberservice.interfaces.dto.request.MemberSignUpRequestDto;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Tier tier;

    @Enumerated(EnumType.STRING)
    private OAuthProvider oAuthProvider;

    public static Member of(String email, String password, String nickname, Role role, Tier tier, OAuthProvider oAuthProvider) {
        return Member.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .role(role)
                .tier(tier)
                .oAuthProvider(oAuthProvider)
                .build();
    }

    public static Member from(MemberSignUpRequestDto dto) {
        return Member.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .nickname(dto.getNickname())
                .role(dto.getRole())
                .tier(Tier.BRONZE)
                .build();
    }
}

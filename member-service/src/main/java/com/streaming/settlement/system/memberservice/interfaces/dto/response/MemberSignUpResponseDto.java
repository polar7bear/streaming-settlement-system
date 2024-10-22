package com.streaming.settlement.system.memberservice.interfaces.dto.response;

import com.streaming.settlement.system.memberservice.domain.core.entity.Member;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberSignUpResponseDto {

    private String email;
    private String nickname;
    private Role role;

    public static MemberSignUpResponseDto from(Member entity) {
        return MemberSignUpResponseDto.builder()
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .role(entity.getRole())
                .build();
    }
}

package com.streaming.settlement.system.memberservice.dto.response;

import com.streaming.settlement.system.memberservice.domain.entity.enums.Role;
import com.streaming.settlement.system.memberservice.domain.entity.enums.Tier;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthMemberResponseDto {
    private String email;
    private String nickname;
    private Role role;
    private Tier tier;
    private String provider;
}

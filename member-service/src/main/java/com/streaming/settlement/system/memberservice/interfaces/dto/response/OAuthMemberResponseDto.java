package com.streaming.settlement.system.memberservice.interfaces.dto.response;

import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Role;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Tier;
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

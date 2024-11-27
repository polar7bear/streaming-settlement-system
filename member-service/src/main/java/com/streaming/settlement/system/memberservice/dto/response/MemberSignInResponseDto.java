package com.streaming.settlement.system.memberservice.dto.response;

import lombok.*;

import java.util.Date;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberSignInResponseDto {

    private String email;
    private String accessToken;
    private Date tokenExpiration;




    public static MemberSignInResponseDto of(String email, String accessToken, Date tokenExpiration) {
        return new MemberSignInResponseDto(email, accessToken, tokenExpiration);
    }
}

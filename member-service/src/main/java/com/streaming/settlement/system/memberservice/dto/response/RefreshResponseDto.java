package com.streaming.settlement.system.memberservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshResponseDto {

    private String email;
    private String accessToken;

    public static RefreshResponseDto of(String email, String accessToken) {
        return new RefreshResponseDto(email, accessToken);
    }
}

package com.streaming.settlement.system.memberservice.config.security.oauth;

import com.streaming.settlement.system.memberservice.dto.response.OAuthMemberResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final OAuthMemberResponseDto userResponseDto;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return String.valueOf(userResponseDto.getRole());
            }
        });
        return list;
    }

    @Override
    public String getName() {
        return userResponseDto.getNickname();
    }

    public String getEmail() {
        return userResponseDto.getEmail();
    }

    public String getProvider() {
        return userResponseDto.getProvider();
    }
}

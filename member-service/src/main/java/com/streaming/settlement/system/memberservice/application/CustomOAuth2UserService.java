package com.streaming.settlement.system.memberservice.application;

import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Role;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Tier;
import com.streaming.settlement.system.memberservice.infrastructure.config.security.oauth.CustomOAuth2User;
import com.streaming.settlement.system.memberservice.interfaces.dto.response.GoogleResponse;
import com.streaming.settlement.system.memberservice.interfaces.dto.response.OAuth2Response;
import com.streaming.settlement.system.memberservice.interfaces.dto.response.UserResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    // TODO: DB 코드
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response response = whichProvider(oAuth2User, registrationId);

        UserResponseDto dto = UserResponseDto.builder()
                .email(response.getEmail())
                .nickname(response.getName())
                .role(Role.MEMBER)
                .tier(Tier.BRONZE)
                .provider(response.getProvider())
                .build();

        return new CustomOAuth2User(dto);
    }

    private OAuth2Response whichProvider(OAuth2User oAuth2User, String provider) {
        // TODO: 네이버 카카오
        return switch (provider) {
            case "google" -> new GoogleResponse(oAuth2User.getAttributes());
            default -> null;
        };
    }
}

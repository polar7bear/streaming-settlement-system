package com.streaming.settlement.system.memberservice.application;

import com.streaming.settlement.system.common.api.exception.member.DuplicateMemberException;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Role;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Tier;
import com.streaming.settlement.system.memberservice.infrastructure.config.security.oauth.CustomOAuth2User;
import com.streaming.settlement.system.memberservice.infrastructure.repository.MemberRepository;
import com.streaming.settlement.system.memberservice.interfaces.dto.response.GoogleResponse;
import com.streaming.settlement.system.memberservice.interfaces.dto.response.OAuth2Response;
import com.streaming.settlement.system.memberservice.interfaces.dto.response.OAuthMemberResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth Login Username: {}", oAuth2User.getName());
        log.info("OAuth Login Attributes: {}", oAuth2User.getAttributes());
        log.info("OAuth Login Authorities: {}", oAuth2User.getAuthorities());
        String email = (String) oAuth2User.getAttributes().get("email");

        memberRepository.findByEmail(email)
                .ifPresent(alreadyExist -> {
                    throw new DuplicateMemberException("이미 가입된 회원입니다: " + email);
                });

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response response = whichProvider(oAuth2User, registrationId);

        OAuthMemberResponseDto dto = OAuthMemberResponseDto.builder()
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

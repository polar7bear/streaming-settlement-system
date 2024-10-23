package com.streaming.settlement.system.memberservice.infrastructure.config.security.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaming.settlement.system.common.api.ApiResponse;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Role;
import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Tier;
import com.streaming.settlement.system.memberservice.interfaces.dto.response.OAuthMemberResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    // 여기서는 소셜로그인 가입전용 페이지로 이동시켜만 주자
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();
        String name = (String) user.getAttributes().get("name");

        OAuthMemberResponseDto data = OAuthMemberResponseDto.builder() // 클라이언트 사이드에서는 닉네임과 role만 추가적으로 설정하여 가입시키자 -> 사용자 정보 제공하는 api로 redirect, 해당 api도 만들어야하네
                .email(user.getEmail())
                .nickname(name)
                .role(Role.MEMBER)
                .tier(Tier.BRONZE)
                .provider(user.getProvider())
                .build();

        ApiResponse<OAuthMemberResponseDto> responseData = new ApiResponse<OAuthMemberResponseDto>("추가정보 입력페이지로 이동합니다.", data);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseData));

        /*Member memberEntity = Member.of(
                user.getEmail(),
                "1234",     // TODO: 소셜로그인 사용자 비밀번호 어떻게 관리할 것인지
                (String) user.getAttributes().get(name),
                Role.MEMBER,
                Tier.BRONZE,
                OAuthProvider.GOOGLE
        );

        memberRepository.save(memberEntity);

        String accessToken = tokenProvider.createAccessToken(authentication);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Cookie cookie = new Cookie("Access-Token", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(900);

        response.addCookie(cookie);


        tokenProvider.createRefreshToken(authentication); // TODO: 리프레쉬는 레디스에 저장 -> 추후 레디스 설정 후에 진행예정 / 블랙리스트 화이트리스트 구현*/


    }
}

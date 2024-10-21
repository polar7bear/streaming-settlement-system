package com.streaming.settlement.system.memberservice.infrastructure.config.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // TODO: 액세스토큰, 리프레쉬 토큰 발급 쿠키에 액세스 토큰 저장 / 리프레쉬는 레디스에 저장 -> 추후 레디스 설정 후에 진행예정

    }
}

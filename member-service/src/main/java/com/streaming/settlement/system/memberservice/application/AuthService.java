package com.streaming.settlement.system.memberservice.application;

import com.streaming.settlement.system.common.api.exception.member.DuplicateMemberException;
import com.streaming.settlement.system.common.api.exception.member.WrongPasswordException;
import com.streaming.settlement.system.memberservice.domain.core.entity.Member;
import com.streaming.settlement.system.memberservice.infrastructure.config.security.jwt.TokenProvider;
import com.streaming.settlement.system.memberservice.infrastructure.repository.MemberRepository;
import com.streaming.settlement.system.memberservice.interfaces.dto.request.MemberSignInRequestDto;
import com.streaming.settlement.system.memberservice.interfaces.dto.request.MemberSignUpRequestDto;
import com.streaming.settlement.system.memberservice.interfaces.dto.response.MemberSignUpResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional
    public MemberSignUpResponseDto signUp(MemberSignUpRequestDto dto) {
        memberRepository.findByEmail(dto.getEmail())
                .ifPresent(existUser -> {
                    throw new DuplicateMemberException("이미 존재하는 회원입니다.");
                });
        String encoded = passwordEncoder.encode(dto.getPassword());
        dto.setPassword(encoded);
        Member entity = Member.from(dto);
        memberRepository.save(entity);

        return MemberSignUpResponseDto.from(entity);
    }

    public String signIn(MemberSignInRequestDto dto, HttpServletResponse response) {
        Member entity = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), entity.getPassword())) {
            throw new WrongPasswordException("비밀번호가 일치하지 않습니다.");
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(entity.getEmail(), entity.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.createAccessToken(authentication);
        Cookie cookie = setCookie(accessToken);
        response.addCookie(cookie);

        // TODO: 레디스 연결 후 리프레쉬토큰을 레디스에 저장
        tokenProvider.createRefreshToken(authentication);
        return "로그인에 성공하였습니다.";
    }

    public Cookie setCookie(String accessToken) {
        Cookie cookie = new Cookie("Access-Token", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(900);

        return cookie;
    }

    public Cookie invalidateCookie() {
        Cookie cookie = new Cookie("Access-Token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        return cookie;
    }
}

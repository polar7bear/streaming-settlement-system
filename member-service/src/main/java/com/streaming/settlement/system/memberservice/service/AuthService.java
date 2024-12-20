package com.streaming.settlement.system.memberservice.service;

import com.streaming.settlement.system.common.api.exception.member.DuplicateMemberException;
import com.streaming.settlement.system.common.api.exception.member.WrongPasswordException;
import com.streaming.settlement.system.memberservice.config.security.CustomUserDetailsService;
import com.streaming.settlement.system.memberservice.config.security.jwt.TokenProvider;
import com.streaming.settlement.system.memberservice.domain.entity.Member;
import com.streaming.settlement.system.memberservice.dto.request.MemberSignInRequestDto;
import com.streaming.settlement.system.memberservice.dto.request.MemberSignUpRequestDto;
import com.streaming.settlement.system.memberservice.dto.response.MemberSignInResponseDto;
import com.streaming.settlement.system.memberservice.dto.response.MemberSignUpResponseDto;
import com.streaming.settlement.system.memberservice.dto.response.RefreshResponseDto;
import com.streaming.settlement.system.memberservice.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String REFRESH_TOKEN = "Refresh-Token";
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisTokenService redisTokenService;
    private final CustomUserDetailsService customUserDetailsService;

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

    public String signInV1(MemberSignInRequestDto dto, HttpServletResponse response) {
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

    public MemberSignInResponseDto signInV2(MemberSignInRequestDto dto, HttpServletResponse response) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.createAccessToken(authentication);
        Date expire = tokenProvider.getTokenExpire(accessToken);

        String refreshToken = tokenProvider.createRefreshToken(authentication);
        Cookie cookie = setCookie(refreshToken);
        response.addCookie(cookie);

        redisTokenService.saveRefreshToken(dto.getEmail(), refreshToken);

        return new MemberSignInResponseDto(dto.getEmail(), accessToken, expire);
    }

    public void signOut(String email, String accessToken, HttpServletResponse response) {
        String substring = accessToken.substring(7);
        redisTokenService.signOut(email, substring);
        response.addCookie(invalidateCookie());
    }

    public RefreshResponseDto refresh(String originRefreshToken, String originAccessToken) {
        String email = tokenProvider.getUsername(originRefreshToken);
        boolean checkRefreshToken = redisTokenService.validateRefreshToken(email, originRefreshToken);

        if (checkRefreshToken) {
            redisTokenService.addAccessTokenToBlackList(originAccessToken);

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            String refreshedAccessToken = tokenProvider.createAccessToken(authentication);

            return RefreshResponseDto.of(email, refreshedAccessToken);
        }
        return null;
    }


    public Cookie setCookie(String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(900);

        return cookie;
    }

    public Cookie invalidateCookie() {
        Cookie cookie = new Cookie(REFRESH_TOKEN, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        return cookie;
    }

    public String getEmailByRefreshTokenCookie(String refreshToken) {
        return tokenProvider.getUsername(refreshToken);
    }
}

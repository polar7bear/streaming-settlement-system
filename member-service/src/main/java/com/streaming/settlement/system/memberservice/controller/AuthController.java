package com.streaming.settlement.system.memberservice.controller;

import com.streaming.settlement.system.common.api.ApiResponse;
import com.streaming.settlement.system.memberservice.service.AuthService;
import com.streaming.settlement.system.memberservice.dto.request.MemberSignInRequestDto;
import com.streaming.settlement.system.memberservice.dto.request.MemberSignUpRequestDto;
import com.streaming.settlement.system.memberservice.dto.request.OAuthMemberSignUpReqeustDto;
import com.streaming.settlement.system.memberservice.dto.response.MemberSignUpResponseDto;
import com.streaming.settlement.system.memberservice.dto.response.OAuthMemberResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/sign-up")
    public ApiResponse<MemberSignUpResponseDto> normalMemberSignUp(@Valid @RequestBody MemberSignUpRequestDto dto) {
        MemberSignUpResponseDto response = authService.signUp(dto);
        return new ApiResponse<>("회원가입이 성공적으로 완료되었습니다.", response);
    }

    @PostMapping("/sign-in")
    public ApiResponse<?> signIn(@Valid @RequestBody MemberSignInRequestDto dto, HttpServletResponse response) {
        String message = authService.signInV2(dto, response);
        return new ApiResponse<>(message);
    }

    @PostMapping("/sign-out")
    public ApiResponse<String> signOut(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = authService.invalidateCookie();
        response.addCookie(cookie);
        // TODO: 액세스토큰 레디스 블랙리스트에 추가, 리프레쉬 토큰 화이트리스트에서 제거
        return new ApiResponse<>("로그아웃 되었습니다.");
    }


    // TODO: 두개는 클라이언트 코드 임시로 작성해서 테스트해보면서 해봐야 할 듯하므로 나중에 진행
    @PostMapping("/oauth/sign-up")
    public String oAuthMemberSignUp(@Valid @RequestBody OAuthMemberSignUpReqeustDto dto) {
        // 토큰발급을 oauthSuccessHandler에서 하지말고 현재 해당 API에서 처리하는게 낫겠다.


        return null;
    }

    @GetMapping("/oauth/additional-info")
    public ApiResponse<OAuthMemberResponseDto> additionalInfo() {
        return null;
    }
}

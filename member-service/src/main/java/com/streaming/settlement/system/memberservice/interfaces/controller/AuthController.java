package com.streaming.settlement.system.memberservice.interfaces.controller;

import com.streaming.settlement.system.common.api.ApiResponse;
import com.streaming.settlement.system.memberservice.application.AuthService;
import com.streaming.settlement.system.memberservice.interfaces.dto.request.MemberSignUpRequestDto;
import com.streaming.settlement.system.memberservice.interfaces.dto.request.OAuthMemberSignUpReqeustDto;
import com.streaming.settlement.system.memberservice.interfaces.dto.response.MemberSignUpResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/oauth/sign-up")
    public String oAuthMemberSignUp(@Valid @RequestBody OAuthMemberSignUpReqeustDto dto) {
        // TODO: 추후에 자체 회원가입 API 만들면, 소셜 로그인을 통해 가입한 회원을 통합시키기 위한 API
        //  -> 이렇게 하려면 토큰발급을 oauthSuccessHandler에서 하지말고 현재 해당 API에서 처리하는게 낫겠다.


        return null;
    }

    @PostMapping("/sign-up")
    public ApiResponse<MemberSignUpResponseDto> normalMemberSignUp(@Valid @RequestBody MemberSignUpRequestDto dto) {
        MemberSignUpResponseDto response = authService.signUp(dto);
        return new ApiResponse<>("회원가입이 성공적으로 완료되었습니다.", response);
    }


    // TODO: 로그인 로그아웃 구현
    @PostMapping("/sign-in")
    public String signIn() {
        return null;
    }

    @PostMapping("/sign-out")
    public ApiResponse<String> signOut() {
        return null;
    }
}

package com.streaming.settlement.system.memberservice.controller;

import com.streaming.settlement.system.common.api.ApiError;
import com.streaming.settlement.system.common.api.ApiErrorType;
import com.streaming.settlement.system.common.api.ApiResponse;
import com.streaming.settlement.system.memberservice.dto.request.MemberSignInRequestDto;
import com.streaming.settlement.system.memberservice.dto.request.MemberSignUpRequestDto;
import com.streaming.settlement.system.memberservice.dto.request.OAuthMemberSignUpReqeustDto;
import com.streaming.settlement.system.memberservice.dto.response.MemberSignInResponseDto;
import com.streaming.settlement.system.memberservice.dto.response.MemberSignUpResponseDto;
import com.streaming.settlement.system.memberservice.dto.response.OAuthMemberResponseDto;
import com.streaming.settlement.system.memberservice.dto.response.RefreshResponseDto;
import com.streaming.settlement.system.memberservice.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/members/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_TOKEN = "Refresh-Token";
    private final AuthService authService;


    @PostMapping("/sign-up")
    public ApiResponse<MemberSignUpResponseDto> normalMemberSignUp(@Valid @RequestBody MemberSignUpRequestDto dto) {
        MemberSignUpResponseDto response = authService.signUp(dto);
        return new ApiResponse<>("회원가입이 성공적으로 완료되었습니다.", response);
    }

    @PostMapping("/sign-in")
    public ApiResponse<MemberSignInResponseDto> signIn(@Valid @RequestBody MemberSignInRequestDto dto, HttpServletResponse response) {
        MemberSignInResponseDto responseDto = authService.signInV2(dto, response);
        return new ApiResponse<>(responseDto);
    }

    @PostMapping("/sign-out")
    public ApiResponse<String> signOut(@RequestHeader(AUTHORIZATION_HEADER) String accessToken, @CookieValue(value = REFRESH_TOKEN) String refreshToken, HttpServletResponse response) {
        String email = authService.getEmailByRefreshTokenCookie(refreshToken);
        authService.signOut(email, accessToken, response);
        return new ApiResponse<>("로그아웃 되었습니다.");
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshResponseDto> refresh(@RequestHeader(AUTHORIZATION_HEADER) String accessToken, @CookieValue(value = REFRESH_TOKEN) String refreshToken) {
        String token = accessToken.substring(7);
        RefreshResponseDto response = authService.refresh(refreshToken, token);

        return response != null
                ? new ApiResponse<>("액세스 토큰이 재발급 되었습니다.", response)
                : new ApiResponse<>("오류가 발생하였습니다..", new ApiError(ApiErrorType.NOT_FOUND, "404", "유효하지 않는 리프레쉬 토큰입니다.."));
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

package com.streaming.settlement.system.memberservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MemberSignInRequestDto {

    @Email(message = "이메일 형식을 지켜주세요.")
    @NotBlank(message = "필수 입력란입니다.")
    @Size(max = 30, message = "30자이내로 입력해주세요.")
    private String email;

    @NotBlank(message = "필수 입력란입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
            message = "영소문자와 특수기호가 1개 이상 포함된 최소 8자 이상, 16자 이하의 길이로 입력해주세요.")
    private String password;




}

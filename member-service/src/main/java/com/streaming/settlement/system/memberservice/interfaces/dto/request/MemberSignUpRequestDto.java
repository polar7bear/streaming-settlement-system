package com.streaming.settlement.system.memberservice.interfaces.dto.request;

import com.streaming.settlement.system.memberservice.domain.core.entity.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberSignUpRequestDto {


    @Email(message = "이메일 형식을 지켜주세요.")
    @NotBlank(message = "필수 입력란입니다.")
    @Size(max = 30, message = "30자이내로 입력해주세요.")
    private String email;

    @NotBlank(message = "필수 입력란입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
            message = "영소문자와 특수기호가 1개 이상 포함된 최소 8자 이상, 16자 이하의 길이로 입력해주세요.")
    private String password;

    @NotBlank
    @Size(min = 2, max = 8, message = "2글자 이상, 8글자 이하로 입력해주세요.")
    private String nickname;

    @NotNull
    private Role role;

    /*@Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "전화번호 형식은 XXX-XXXX-XXXX이어야 합니다.")
    private String tel;*/


}

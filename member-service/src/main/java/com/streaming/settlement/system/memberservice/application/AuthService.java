package com.streaming.settlement.system.memberservice.application;

import com.streaming.settlement.system.common.api.exception.member.DuplicateMemberException;
import com.streaming.settlement.system.memberservice.domain.core.entity.Member;
import com.streaming.settlement.system.memberservice.infrastructure.config.security.jwt.TokenProvider;
import com.streaming.settlement.system.memberservice.infrastructure.repository.MemberRepository;
import com.streaming.settlement.system.memberservice.interfaces.dto.request.MemberSignUpRequestDto;
import com.streaming.settlement.system.memberservice.interfaces.dto.response.MemberSignUpResponseDto;
import lombok.RequiredArgsConstructor;
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

}

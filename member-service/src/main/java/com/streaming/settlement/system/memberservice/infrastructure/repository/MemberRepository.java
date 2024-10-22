package com.streaming.settlement.system.memberservice.infrastructure.repository;

import com.streaming.settlement.system.memberservice.domain.core.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(Object email);
}

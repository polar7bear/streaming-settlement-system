package com.streaming.settlement.system.streamingadservice.repository;

import com.streaming.settlement.system.streamingadservice.domain.entity.AdViewLog;
import com.streaming.settlement.system.streamingadservice.domain.entity.StreamingAdMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdViewLogRepository extends JpaRepository<AdViewLog, Long> {

    @Query("""
                SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
                FROM AdViewLog a
                WHERE a.mapping = :mapping
                  AND a.ipAddress = :ipAddress
                  AND a.memberId = :memberId
            """)
    boolean existsByMappingAndIpAddressAndMemberId(StreamingAdMapping mapping, String ipAddress, Long memberId);

    @Query("""
                SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
                FROM AdViewLog a
                WHERE a.mapping = :mapping
                  AND a.ipAddress = :ipAddress
                  AND a.memberId IS NULL
            """)
    boolean existsByMappingAndIpAddressAndMemberIdIsNull(StreamingAdMapping mapping, String ipAddress);
}

package com.streaming.settlement.system.streamingadservice.repository;

import com.streaming.settlement.system.streamingadservice.domain.entity.AdViewLog;
import com.streaming.settlement.system.streamingadservice.domain.entity.Advertisement;
import com.streaming.settlement.system.streamingadservice.domain.entity.StreamingAdMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdViewLogRepository extends JpaRepository<AdViewLog, Long> {
    boolean existsByMappingAndIpAddressAndMemberId(StreamingAdMapping mapping, String ipAddress, Long memberId);
}

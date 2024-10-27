package com.streaming.settlement.system.streamingadservice.repository;

import com.streaming.settlement.system.streamingadservice.domain.entity.AdViewLog;
import com.streaming.settlement.system.streamingadservice.domain.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdViewLogRepository extends JpaRepository<AdViewLog, Long> {
    boolean existsByAdvertisementAndIpAddressAndMemberId(Advertisement ad, String ipAddress, Long memberId);
}

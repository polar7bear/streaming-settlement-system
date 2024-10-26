package com.streaming.settlement.system.streamingadservice.repository;

import com.streaming.settlement.system.streamingadservice.domain.entity.StreamingViewLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface StreamingViewLogRepository extends JpaRepository<StreamingViewLog, Long> {
    Optional<StreamingViewLog> findByMemberIdAndStreamingId(Long memberId, Long streamingId);

    boolean existsByStreamingIdAndIpAddressAndViewedAtAfter(Long streamingId, String ipAddress, LocalDateTime thirtySecondsAgo);
}

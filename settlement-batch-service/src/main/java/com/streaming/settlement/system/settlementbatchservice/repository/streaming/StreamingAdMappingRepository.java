package com.streaming.settlement.system.settlementbatchservice.repository.streaming;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming.StreamingAdMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StreamingAdMappingRepository extends JpaRepository<StreamingAdMapping, Long> {
    List<StreamingAdMapping> findByStreamingId(Long streamingId);
}

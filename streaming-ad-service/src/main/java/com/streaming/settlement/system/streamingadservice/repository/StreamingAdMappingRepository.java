package com.streaming.settlement.system.streamingadservice.repository;

import com.streaming.settlement.system.streamingadservice.domain.entity.StreamingAdMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StreamingAdMappingRepository extends JpaRepository<StreamingAdMapping, Long> {
    List<StreamingAdMapping> findByStreamingId(Long id);
}

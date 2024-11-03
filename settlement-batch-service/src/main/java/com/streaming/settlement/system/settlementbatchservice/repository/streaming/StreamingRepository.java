package com.streaming.settlement.system.settlementbatchservice.repository.streaming;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming.Streaming;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface StreamingRepository extends JpaRepository<Streaming, Long> {

    Page<Streaming> findByCreatedAtBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}

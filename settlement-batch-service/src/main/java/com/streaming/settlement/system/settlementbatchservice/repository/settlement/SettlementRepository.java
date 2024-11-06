package com.streaming.settlement.system.settlementbatchservice.repository.settlement;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findTopByStreamingIdOrderBySettlementEndDateDesc(Long id);

    Optional<Settlement> findByStreamingIdAndSettlementStartDateAndSettlementEndDate(Long id, LocalDateTime startDate, LocalDateTime endDate);
}

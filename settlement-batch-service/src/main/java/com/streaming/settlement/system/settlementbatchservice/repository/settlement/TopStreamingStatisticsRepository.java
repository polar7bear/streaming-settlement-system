package com.streaming.settlement.system.settlementbatchservice.repository.settlement;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.TopStreamingStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopStreamingStatisticsRepository extends JpaRepository<TopStreamingStatistics, Long> {
}

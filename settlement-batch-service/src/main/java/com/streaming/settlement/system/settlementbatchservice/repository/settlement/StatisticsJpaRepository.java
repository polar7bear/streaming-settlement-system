package com.streaming.settlement.system.settlementbatchservice.repository.settlement;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatisticsJpaRepository extends JpaRepository<Statistics, Long> {
}

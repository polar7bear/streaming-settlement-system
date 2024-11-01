package com.streaming.settlement.system.settlementbatchservice.repository.settlement;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.StatisticsSummary;
import com.streaming.settlement.system.settlementbatchservice.domain.enums.DateRange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StatisticsSummaryRepository extends JpaRepository<StatisticsSummary, Long> {
    Optional<StatisticsSummary> findByDateRangeAndTargetDate(DateRange dateRange, LocalDate localDate);
}

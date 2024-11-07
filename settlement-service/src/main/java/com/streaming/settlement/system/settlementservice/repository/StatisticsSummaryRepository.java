package com.streaming.settlement.system.settlementservice.repository;

import com.streaming.settlement.system.settlementservice.domain.entity.StatisticsSummary;
import com.streaming.settlement.system.settlementservice.domain.enums.DateRange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StatisticsSummaryRepository extends JpaRepository<StatisticsSummary, Long> {
    Optional<StatisticsSummary> findByTargetDateAndDateRange(LocalDate date, DateRange dateRange);
}

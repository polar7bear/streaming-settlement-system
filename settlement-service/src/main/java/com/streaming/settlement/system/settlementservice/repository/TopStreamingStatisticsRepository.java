package com.streaming.settlement.system.settlementservice.repository;

import com.streaming.settlement.system.settlementservice.domain.entity.StatisticsSummary;
import com.streaming.settlement.system.settlementservice.domain.entity.TopStreamingStatistics;
import com.streaming.settlement.system.settlementservice.domain.enums.StatisticsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TopStreamingStatisticsRepository extends JpaRepository<TopStreamingStatistics, Long> {
    List<TopStreamingStatistics> findByStatisticsSummaryAndStatisticsTypeOrderByRankingAsc(StatisticsSummary summary, StatisticsType statisticsType);

    @Query("SELECT t FROM TopStreamingStatistics t " +
            "WHERE t.statisticsSummary = :summary " +
            "ORDER BY t.statisticsType ASC, t.ranking ASC")
    List<TopStreamingStatistics> findByStatisticsSummaryOrderByStatisticsTypeAscRankingAsc(StatisticsSummary summary);
}

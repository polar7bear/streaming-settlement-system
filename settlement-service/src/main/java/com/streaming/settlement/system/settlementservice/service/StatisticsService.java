package com.streaming.settlement.system.settlementservice.service;

import com.streaming.settlement.system.common.api.exception.statistics.NotFoundStatisticsException;
import com.streaming.settlement.system.settlementservice.domain.entity.StatisticsSummary;
import com.streaming.settlement.system.settlementservice.domain.entity.TopStreamingStatistics;
import com.streaming.settlement.system.settlementservice.domain.enums.DateRange;
import com.streaming.settlement.system.settlementservice.domain.enums.StatisticsType;
import com.streaming.settlement.system.settlementservice.dto.response.statistics.StatisticsResponseDto;
import com.streaming.settlement.system.settlementservice.dto.response.statistics.TopStreamingDto;
import com.streaming.settlement.system.settlementservice.repository.StatisticsSummaryRepository;
import com.streaming.settlement.system.settlementservice.repository.TopStreamingStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {


    private final StatisticsSummaryRepository statisticsSummaryRepository;
    private final TopStreamingStatisticsRepository topStreamingStatisticsRepository;

    public StatisticsResponseDto getStatisticsByTwoQuery(LocalDate date, DateRange dateRange) {
        LocalDate targetDate = getTargetDate(date, dateRange);

        StatisticsSummary summary = statisticsSummaryRepository.findByTargetDateAndDateRange(targetDate, dateRange)
                .orElseThrow(() -> new NotFoundStatisticsException("통계 데이터가 존재하지 않습니다."));

        List<TopStreamingDto> viewsRanking = topStreamingStatisticsRepository
                .findByStatisticsSummaryAndStatisticsTypeOrderByRankingAsc(summary, StatisticsType.VIEWS)
                .stream()
                .map(TopStreamingDto::from)
                .toList();

        List<TopStreamingDto> playTimeRanking = topStreamingStatisticsRepository
                .findByStatisticsSummaryAndStatisticsTypeOrderByRankingAsc(summary, StatisticsType.PLAY_TIME)
                .stream()
                .map(TopStreamingDto::from)
                .toList();

        return StatisticsResponseDto.builder()
                .dateRange(dateRange)
                .targetDate(targetDate)
                .viewsRanking(viewsRanking)
                .playTimeRanking(playTimeRanking)
                .build();
    }

    public StatisticsResponseDto getStatisticsByOneQueryWithGrouping(LocalDate date, DateRange dateRange) {
        LocalDate targetDate = getTargetDate(date, dateRange);

        StatisticsSummary summary = statisticsSummaryRepository.findByTargetDateAndDateRange(targetDate, dateRange)
                .orElseThrow(() -> new NotFoundStatisticsException("통계 데이터가 존재하지 않습니다."));

        List<TopStreamingStatistics> allStats = topStreamingStatisticsRepository
                .findByStatisticsSummaryOrderByStatisticsTypeAscRankingAsc(summary);

        Map<StatisticsType, List<TopStreamingDto>> map = allStats.stream()
                .map(TopStreamingDto::from)
                .collect(Collectors.groupingBy(TopStreamingDto::getStatisticsType, Collectors.toList()));

        return StatisticsResponseDto.builder()
                .dateRange(dateRange)
                .targetDate(targetDate)
                .viewsRanking(map.getOrDefault(StatisticsType.VIEWS, Collections.emptyList()))
                .playTimeRanking(map.getOrDefault(StatisticsType.PLAY_TIME, Collections.emptyList()))
                .build();
    }

    private static LocalDate getTargetDate(LocalDate date, DateRange dateRange) {
        return switch (dateRange) {
            case WEEKLY -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY -> YearMonth.from(date).atDay(1);
            case DAILY -> date;
        };
    }
}

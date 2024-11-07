package com.streaming.settlement.system.settlementservice.controller;

import com.streaming.settlement.system.common.api.ApiResponse;
import com.streaming.settlement.system.settlementservice.domain.enums.DateRange;
import com.streaming.settlement.system.settlementservice.dto.response.statistics.StatisticsResponseDto;
import com.streaming.settlement.system.settlementservice.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {


    private final StatisticsService statisticsService;

    @GetMapping("/daily")
    public ApiResponse<StatisticsResponseDto> getDailyStatistics(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date) {
        //StatisticsResponseDto response = statisticsService.getStatisticsByTwoQuery(date, DateRange.DAILY);
        StatisticsResponseDto response = statisticsService.getStatisticsByTwoQuery(date, DateRange.DAILY);
        return new ApiResponse<>(date + " 해당 날짜의 일별 데이터 조회 성공", response);
    }

    @GetMapping("/weekly")
    public ApiResponse<StatisticsResponseDto> getWeeklyStatistics(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date) {
        StatisticsResponseDto response = statisticsService.getStatisticsByTwoQuery(date, DateRange.WEEKLY);
        //StatisticsResponseDto response = statisticsService.getStatisticsByTwoQuery(date, DateRange.WEEKLY);
        return new ApiResponse<>(date + " 해당 날짜의 주별 데이터 조회 성공", response);
    }

    @GetMapping("/monthly")
    public ApiResponse<StatisticsResponseDto> getMonthlyStatistics(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        StatisticsResponseDto response = statisticsService.getStatisticsByTwoQuery(yearMonth.atDay(1), DateRange.MONTHLY);
        //StatisticsResponseDto response = statisticsService.getStatisticsByTwoQuery(yearMonth.atDay(1), DateRange.MONTHLY);
        return new ApiResponse<>(yearMonth + " 해당 날짜의 월별 데이터 조회 성공", response);
    }
}

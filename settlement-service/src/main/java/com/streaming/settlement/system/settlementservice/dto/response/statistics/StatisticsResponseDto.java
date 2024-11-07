package com.streaming.settlement.system.settlementservice.dto.response.statistics;

import com.streaming.settlement.system.settlementservice.domain.enums.DateRange;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class StatisticsResponseDto {

    private DateRange dateRange;
    private LocalDate targetDate;
    private List<TopStreamingDto> viewsRanking;
    private List<TopStreamingDto> playTimeRanking;
}

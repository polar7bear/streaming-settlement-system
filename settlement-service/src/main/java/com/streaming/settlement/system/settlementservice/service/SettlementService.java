package com.streaming.settlement.system.settlementservice.service;

import com.streaming.settlement.system.settlementservice.domain.entity.Settlement;
import com.streaming.settlement.system.settlementservice.dto.response.settlement.Detail;
import com.streaming.settlement.system.settlementservice.dto.response.settlement.SettlementResponseDto;
import com.streaming.settlement.system.settlementservice.dto.response.settlement.StreamingSettlement;
import com.streaming.settlement.system.settlementservice.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;

    public SettlementResponseDto getDailySettlement(Long memberId, LocalDate date) {
        List<Settlement> settlements = settlementRepository.findByMemberIdAndDate(memberId, date);
        return createSettlementResponseDto(settlements);
    }

    public SettlementResponseDto getWeeklySettlement(Long memberId, LocalDate date) {
        LocalDate startDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusDays(6);
        List<Settlement> settlements = settlementRepository.findByMemberIdAndDateBetween(memberId, startDate, endDate);
        return createSettlementResponseDto(settlements);
    }

    public SettlementResponseDto getMonthlySettlement(Long memberId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<Settlement> settlements = settlementRepository.findByMemberIdAndDateBetween(memberId, startDate, endDate);
        return createSettlementResponseDto(settlements);
    }

    private SettlementResponseDto createSettlementResponseDto(List<Settlement> settlements) {
        if (settlements.isEmpty()) return createEmptyResponse();

        List<StreamingSettlement> streamingResults = settlements.stream()
                .map(settlement -> {
                    return StreamingSettlement.builder()
                            .streamingId(settlement.getStreamingId())
                            .streamingTitle("영상 " + settlement.getStreamingId())
                            .totalAmount(settlement.getTotalRevenue())
                            .detail(Detail.builder()
                                    .streamingRevenue(settlement.getStreamingRevenue())
                                    .adRevenue(settlement.getAdRevenue())
                                    .build())
                            .build();
                })
                .sorted(Comparator.comparing(StreamingSettlement::getTotalAmount).reversed())
                .toList();

        BigDecimal allStreamingTotalAmount = streamingResults.stream()
                .map(StreamingSettlement::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SettlementResponseDto.builder()
                .totalAmount(allStreamingTotalAmount)
                .streamingSettlements(streamingResults)
                .build();
    }

    private SettlementResponseDto createEmptyResponse() {
        return SettlementResponseDto.builder()
                .totalAmount(BigDecimal.ZERO)
                .streamingSettlements(Collections.emptyList())
                .build();
    }
}

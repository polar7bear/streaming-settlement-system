package com.streaming.settlement.system.settlementservice.controller;

import com.streaming.settlement.system.common.api.ApiResponse;
import com.streaming.settlement.system.settlementservice.dto.response.settlement.SettlementResponseDto;
import com.streaming.settlement.system.settlementservice.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/daily")
    public ApiResponse<SettlementResponseDto> getDailySettlement(
            //@RequestHeader(value = "X-Member-Id") Long memberId,
            @RequestParam Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        SettlementResponseDto response = settlementService.getDailySettlement(memberId, date);
        return new ApiResponse<>(date + " 해당 일의 정산데이터 조회 성공", response);
    }

    @GetMapping("/weekly")
    public ApiResponse<SettlementResponseDto> getWeeklySettlement(
            //@RequestHeader(value = "X-Member-Id") Long memberId,
            @RequestParam Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        SettlementResponseDto response = settlementService.getWeeklySettlement(memberId, date);
        return new ApiResponse<>(date + " 해당 주의 정산데이터 조회 성공", response);
    }

    @GetMapping("/monthly")
    public ApiResponse<SettlementResponseDto> getMonthlySettlement(
            //@RequestHeader(value = "X-Member-Id") Long memberId,
            @RequestParam Long memberId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
    ) {
        SettlementResponseDto response = settlementService.getMonthlySettlement(memberId, yearMonth);
        return new ApiResponse<>(yearMonth + " 해당 월의 정산데이터 조회 성공", response);
    }

}

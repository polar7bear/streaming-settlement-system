package com.streaming.settlement.system.settlementservice.dto.response.settlement;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class SettlementResponseDto {
    private BigDecimal totalAmount;
    private List<StreamingSettlement> streamingSettlements;
}

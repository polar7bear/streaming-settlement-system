package com.streaming.settlement.system.settlementservice.dto.response.settlement;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class Detail {
    private BigDecimal streamingRevenue;
    private BigDecimal adRevenue;
}

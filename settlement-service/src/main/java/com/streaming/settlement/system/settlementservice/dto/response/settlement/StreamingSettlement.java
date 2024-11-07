package com.streaming.settlement.system.settlementservice.dto.response.settlement;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class StreamingSettlement {

    private Long streamingId;
    private String streamingTitle;
    private BigDecimal totalAmount;
    private Detail detail;
}

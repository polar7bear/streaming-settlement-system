package com.streaming.settlement.system.settlementservice.domain.entity;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import com.streaming.settlement.system.settlementservice.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long streamingId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long streamingViews;

    @Column(nullable = false)
    private Long increasedStreamingViews;

    @Column(name = "ad_view_count", nullable = false)
    private Long adViewCount;

    @Column(name = "increased_ad_view_count", nullable = false)
    private Long increasedAdViewCount;

    @Column(nullable = false)
    private BigDecimal streamingRevenue;

    @Column(nullable = false)
    private BigDecimal adRevenue;

    @Column(nullable = false)
    private BigDecimal totalRevenue;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;


}

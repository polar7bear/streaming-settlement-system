package com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming.Streaming;
import com.streaming.settlement.system.settlementbatchservice.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Builder
@AllArgsConstructor
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


    public static Settlement of(Streaming item, Long todayViews, Long todayAdViews, BigDecimal streamingRevenue, BigDecimal adRevenue, BigDecimal totalRevenue) {
        return Settlement.builder()
                .streamingId(item.getId())
                .memberId(item.getMemberId())
                .streamingViews(item.getViews())
                .increasedStreamingViews(todayViews)
                .adViewCount(item.getAdViewCount())
                .increasedAdViewCount(todayAdViews)
                .streamingRevenue(streamingRevenue)
                .adRevenue(adRevenue)
                .totalRevenue(totalRevenue)
                .status(Status.COMPLETED)
                .settlementDate(LocalDate.now())
                .build();
    }

}


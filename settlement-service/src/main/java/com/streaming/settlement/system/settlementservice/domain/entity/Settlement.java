package com.streaming.settlement.system.settlementservice.domain.entity;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import com.streaming.settlement.system.settlementservice.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal streamingRevenue;

    @Column(nullable = false)
    private BigDecimal adRevenue;

    @Column(nullable = false)
    private BigDecimal totalRevenue;

    @Column(nullable = false)
    private Long streamingViews;

    @ElementCollection
    @CollectionTable(name = "settlement_ad_views",
            joinColumns = @JoinColumn(name = "settlement_id"))
    @MapKeyColumn(name = "streaming_ad_mapping_id")
    @Column(name = "views")
    private Map<Long, Long> adViews = new HashMap<>();  // 광고별 누적 조회수

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "settlement_start_date", nullable = false)
    private LocalDateTime settlementStartDate;

    @Column(name = "settlement_end_date", nullable = false)
    private LocalDateTime settlementEndDate;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long streamingId;

}

package com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import com.streaming.settlement.system.settlementbatchservice.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private BigDecimal streamingRevenue;

    @Column(nullable = false)
    private BigDecimal adRevenue;

    @Column(nullable = false)
    private BigDecimal totalRevenue;

    @Column(nullable = false)
    private Long views;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDate settlementDate;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long streamingId;

}


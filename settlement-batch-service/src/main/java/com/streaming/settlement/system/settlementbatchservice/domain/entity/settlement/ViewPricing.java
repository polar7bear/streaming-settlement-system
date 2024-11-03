package com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ViewPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long minViews;

    private Long maxViews;

    @Column(nullable = false)
    private BigDecimal streamRate;

    @Column(nullable = false)
    private BigDecimal adRate;
}

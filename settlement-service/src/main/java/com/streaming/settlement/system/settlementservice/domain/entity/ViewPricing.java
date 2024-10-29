package com.streaming.settlement.system.settlementservice.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ViewPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long minViews;

    @Column(nullable = false)
    private Long maxViews;

    @Column(nullable = false)
    private BigDecimal streamRate;

    @Column(nullable = false)
    private BigDecimal adRate;
}

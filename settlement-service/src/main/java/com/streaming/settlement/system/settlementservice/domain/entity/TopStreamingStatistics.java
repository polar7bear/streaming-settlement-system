package com.streaming.settlement.system.settlementservice.domain.entity;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import com.streaming.settlement.system.settlementservice.domain.enums.StatisticsType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "top_streaming_statistics")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopStreamingStatistics extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statistics_summary_id", nullable = false)
    private StatisticsSummary statisticsSummary;

    @Column(nullable = false)
    private Long streamingId;

    @Column(nullable = false)
    private Long views;

    @Column(nullable = false)
    private Integer totalPlayTime;

    @Column(nullable = false)
    private Integer ranking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatisticsType statisticsType; // VIEWS or PLAY_TIME

}

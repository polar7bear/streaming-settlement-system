package com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "streaming")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Streaming extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_length", nullable = false)
    private Integer totalLength;

    @Column(nullable = false)
    private Long views = 0L;

    @Column(name = "ad_view_count", nullable = false)
    private Long adViewCount = 0L;

    @Column(name = "acc_play_time", nullable = false)
    private Integer accPlayTime = 0;

    @Column(name = "last_settlement_views", nullable = false)
    private Long lastSettlementViews = 0L;

    @Column(name = "last_settlement_ad_count", nullable = false)
    private Long lastSettlementAdCount = 0L;

    @Column(name = "last_settlement_date")
    private LocalDateTime lastSettlementDate;

    @Column(nullable = false)
    private Long memberId;

    public boolean hasViewChanges() {
        return views > lastSettlementViews || adViewCount > lastSettlementAdCount;
    }

    public void updateLastSettlementInfo() {
        this.lastSettlementViews = this.views;
        this.lastSettlementAdCount = this.adViewCount;
        this.lastSettlementDate = LocalDateTime.now();
    }

}


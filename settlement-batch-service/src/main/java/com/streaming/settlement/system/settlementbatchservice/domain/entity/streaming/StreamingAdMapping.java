package com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "streaming_ad_mapping")
public class StreamingAdMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "streaming_id", nullable = false)
    private Streaming streaming;

    @ManyToOne
    @JoinColumn(name = "ad_id", nullable = false)
    private Advertisement advertisement;

    @Column(nullable = false)
    private Integer playTime; // 광고 재생시점 (초단위)

    @Column(nullable = false)
    private Long views = 0L; // 광고 몇번재생되었는지? (조회수)

    @Column(name = "last_settled_count", nullable = false)
    private Long lastSettledCount = 0L;

    @Column(name = "last_settlement_date", nullable = false)
    private LocalDate lastSettlementDate;

    public Long getDailyViewCount() {
        return views - lastSettledCount;
    }

    public void updateSettlement() {
        this.lastSettledCount = this.views;
        this.lastSettlementDate = LocalDate.now();
    }

    public void incrementCount() {
        this.views += 1;
    }
}

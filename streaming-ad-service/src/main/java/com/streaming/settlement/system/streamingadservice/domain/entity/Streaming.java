package com.streaming.settlement.system.streamingadservice.domain.entity;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@Builder
@Table(indexes = {
        @Index(name = "idx_streaming_settlement", columnList = "last_settlement_date, views, ad_view_count"),
        @Index(name = "idx_streaming_update", columnList = "id, last_settlement_views, last_settlement_ad_count")
})
@AllArgsConstructor
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

    @OneToMany(mappedBy = "streaming")
    private List<StreamingAdMapping> streamingAdMappings;


    public void incrementViews() {
        this.views += 1L;
    }

    public void incrementAdViewCount() {
        this.adViewCount += 1L;
    }
}

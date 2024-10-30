package com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement;

import com.streaming.settlement.system.settlementbatchservice.domain.enums.DateRange;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DateRange dateRange;

    @Type(JsonType.class)
    @Column(name = "top_total_play_time_streaming_id", columnDefinition = "json", nullable = false)
    private Map<String, Object> topTotalPlayTimeStreams;

    @Type(JsonType.class)
    @Column(name = "top_views_streaming_id", columnDefinition = "json", nullable = false)
    private Map<String, Object> topViewsStreams;

    @Column(nullable = false)
    private LocalDate createdAt;
}

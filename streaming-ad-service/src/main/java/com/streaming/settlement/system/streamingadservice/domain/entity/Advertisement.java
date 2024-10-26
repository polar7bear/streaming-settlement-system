package com.streaming.settlement.system.streamingadservice.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ad_play_time", nullable = false)
    private Integer adPlayTime = 0; // 광고 삽입시간대

    @Column(nullable = false)
    private Integer count = 0; // 광고 몇번재생되었는지? (조회수)

    @Column(name = "ad_revenue")
    private BigDecimal adRevenue;

    @OneToMany(mappedBy = "advertisement")
    private List<StreamingAdMapping> streamingAdMappings;

    /*@ManyToMany(mappedBy = "advertisements")
    private List<Streaming> streams;*/

    public void incrementCount() {
        this.count += 1;
    }
}

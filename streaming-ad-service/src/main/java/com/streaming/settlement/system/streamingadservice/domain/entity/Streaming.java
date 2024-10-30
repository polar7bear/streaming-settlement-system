package com.streaming.settlement.system.streamingadservice.domain.entity;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Entity
@Builder
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

    /*@Column(name = "last_play_time", nullable = false)
    private Integer LastPlayTime;*/

    @Column(nullable = false)
    private Boolean isSettled = false;

    @Column(name = "acc_play_time", nullable = false)
    private Integer accPlayTime = 0;

    private Long memberId;

    @OneToMany(mappedBy = "streaming")
    private List<StreamingAdMapping> streamingAdMappings;

    /*@ManyToMany
    @JoinTable(
            name = "streaming_ad_mapping",
            joinColumns = @JoinColumn(name = "streaming_id"),
            inverseJoinColumns = @JoinColumn(name = "ad_id")
    )
    private List<Advertisement> advertisements;*/


    public void incrementViews() {
        this.views += 1L;
    }
}

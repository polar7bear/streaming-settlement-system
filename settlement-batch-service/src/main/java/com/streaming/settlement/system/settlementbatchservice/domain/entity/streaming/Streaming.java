package com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    private Boolean isSettled = false;

    @Column(name = "acc_play_time", nullable = false)
    private Integer accPlayTime = 0;

    private Long memberId;

}


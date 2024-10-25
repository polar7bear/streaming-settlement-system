package com.streaming.settlement.system.streamingadservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StreamingViewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "last_play_time", nullable = false)
    private Integer lastPlayTime; //마지막 재생시간 저장하기 위함

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt; // 어뷰징방지

    @JoinColumn(name = "streaming_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Streaming streaming;

    //@Column(name = "member_id", nullable = false) //비회원이 시청할 경우도 있기에 이 컬럼의 not null 유무를 true로 해야 될 것 같은데?
    @Column(name = "member_id")
    private Long memberId;
}

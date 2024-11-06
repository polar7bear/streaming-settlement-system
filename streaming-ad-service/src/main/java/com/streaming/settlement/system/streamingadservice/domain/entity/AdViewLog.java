package com.streaming.settlement.system.streamingadservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdViewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @JoinColumn(name = "mapping_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private StreamingAdMapping mapping;

    //@Column(name = "member_id", nullable = false) //이것도 마찬가지로 비회원 시청가능성때문에 null 가능
    @Column(name = "member_id")
    private Long memberId;

}

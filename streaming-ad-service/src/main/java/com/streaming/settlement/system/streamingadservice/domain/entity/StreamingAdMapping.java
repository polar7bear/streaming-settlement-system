package com.streaming.settlement.system.streamingadservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "streaming_ad_mapping")
public class StreamingAdMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "streaming_id", nullable = false)
    private Streaming streaming;

    @ManyToOne
    @JoinColumn(name = "ad_id", nullable = false)
    private Advertisement advertisement;
}

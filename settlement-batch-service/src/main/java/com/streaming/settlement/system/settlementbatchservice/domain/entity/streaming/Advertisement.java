package com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming;

import com.streaming.settlement.system.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Advertisement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "advertisement")
    private List<StreamingAdMapping> streamingAdMappings;



}

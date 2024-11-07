package com.streaming.settlement.system.settlementservice.dto.response.statistics;

import com.streaming.settlement.system.settlementservice.domain.entity.TopStreamingStatistics;
import com.streaming.settlement.system.settlementservice.domain.enums.StatisticsType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopStreamingDto {
    private Long streamingId;
    private StatisticsType statisticsType;
    private String title;
    private Integer ranking;
    private Long views;
    private Integer totalPlayTime;

    public static TopStreamingDto from(TopStreamingStatistics entity) {
        return TopStreamingDto.builder()
                .streamingId(entity.getStreamingId())
                .statisticsType(entity.getStatisticsType())
                .title("영상 " + entity.getStreamingId())
                .ranking(entity.getRanking())
                .views(entity.getViews())
                .totalPlayTime(entity.getTotalPlayTime())
                .build();
    }
}

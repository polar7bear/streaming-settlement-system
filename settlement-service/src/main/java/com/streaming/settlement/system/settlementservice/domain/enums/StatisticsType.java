package com.streaming.settlement.system.settlementservice.domain.enums;

import lombok.Getter;

@Getter
public enum StatisticsType {
    VIEWS("조회수"),
    PLAY_TIME("재생시간");

    private final String description;

    StatisticsType(String description) {
        this.description = description;
    }
}

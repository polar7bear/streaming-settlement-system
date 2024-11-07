package com.streaming.settlement.system.common.api.exception.statistics;

public class NotFoundStatisticsException extends RuntimeException {

    public NotFoundStatisticsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundStatisticsException(String message) {
        super(message);
    }
}

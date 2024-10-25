package com.streaming.settlement.system.common.api.exception.streaming;

public class NotFoundStreamingViewLogException extends RuntimeException {
    public NotFoundStreamingViewLogException(String message) {
        super(message);
    }

    public NotFoundStreamingViewLogException(String message, Throwable cause) {
        super(message, cause);
    }
}

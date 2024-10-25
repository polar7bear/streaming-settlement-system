package com.streaming.settlement.system.common.api.exception.streaming;

public class NotFoundStreamingException extends RuntimeException {
    public NotFoundStreamingException(String message) {
        super(message);
    }

    public NotFoundStreamingException(String message, Throwable cause) {
        super(message, cause);
    }
}

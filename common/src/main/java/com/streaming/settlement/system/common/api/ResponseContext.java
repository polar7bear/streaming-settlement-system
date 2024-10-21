package com.streaming.settlement.system.common.api;

public class ResponseContext {
    public static ThreadLocal<Long> requestAt = new ThreadLocal<>();

    public static void clear() {
        requestAt.remove();
    }
}

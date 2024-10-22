package com.streaming.settlement.system.common.api;

import lombok.Setter;

public class ResponseContext {
    public static ThreadLocal<Long> requestAt = ThreadLocal.withInitial(() -> null);

    public static void setRequestAt(long time) {
        requestAt.set(time);
    }

    public static void clear() {
        requestAt.remove();
    }
}

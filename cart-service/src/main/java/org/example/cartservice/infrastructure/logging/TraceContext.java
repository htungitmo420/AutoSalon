package org.example.cartservice.infrastructure.logging;

import org.slf4j.MDC;

import java.util.UUID;

public final class TraceContext {

    public static final String TRACE_ID = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private TraceContext() {
    }

    public static String currentTraceId() {
        String traceId = MDC.get(TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID, traceId);
        }
        return traceId;
    }

    public static void runWithTraceId(String traceId, Runnable action) {
        String previousTraceId = MDC.get(TRACE_ID);
        try {
            MDC.put(TRACE_ID, traceId == null || traceId.isBlank() ? UUID.randomUUID().toString() : traceId);
            action.run();
        } finally {
            if (previousTraceId == null) {
                MDC.remove(TRACE_ID);
            } else {
                MDC.put(TRACE_ID, previousTraceId);
            }
        }
    }
}

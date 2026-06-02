package org.example.paymentservice.infrastructure.logging;

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
}

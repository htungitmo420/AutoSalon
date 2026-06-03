package org.example.notificationservice.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DashboardMetricResponse(
        String metricKey,
        long eventCount,
        BigDecimal totalAmount,
        Instant updatedAt
) {
}

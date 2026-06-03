package org.example.notificationservice.application.repository;

import org.example.notificationservice.domain.dashboard.DashboardMetric;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DashboardMetricRepository {
    void increment(String metricKey, BigDecimal amount, Instant updatedAt);
    Optional<DashboardMetric> findById(String metricKey);
    List<DashboardMetric> findAll();
}

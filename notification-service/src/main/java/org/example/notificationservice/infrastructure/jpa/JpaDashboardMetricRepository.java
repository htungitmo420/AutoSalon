package org.example.notificationservice.infrastructure.jpa;

import org.example.notificationservice.domain.dashboard.DashboardMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;

public interface JpaDashboardMetricRepository extends JpaRepository<DashboardMetric, String> {
    @Modifying
    @Query(value = """
            INSERT INTO auto_salon.dashboard_metrics(metric_key, event_count, total_amount, updated_at)
            VALUES (:metricKey, 1, :amount, :updatedAt)
            ON CONFLICT (metric_key) DO UPDATE
            SET event_count = auto_salon.dashboard_metrics.event_count + 1,
                total_amount = auto_salon.dashboard_metrics.total_amount + EXCLUDED.total_amount,
                updated_at = EXCLUDED.updated_at
            """, nativeQuery = true)
    void increment(
            @Param("metricKey") String metricKey,
            @Param("amount") BigDecimal amount,
            @Param("updatedAt") Instant updatedAt);
}

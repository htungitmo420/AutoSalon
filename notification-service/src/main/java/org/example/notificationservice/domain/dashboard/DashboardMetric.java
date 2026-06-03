package org.example.notificationservice.domain.dashboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "dashboard_metrics", schema = "auto_salon")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetric {

    @Id
    @Column(name = "metric_key", nullable = false)
    private String metricKey;

    @Column(name = "event_count", nullable = false)
    private long eventCount;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void increment(BigDecimal amount, Instant now) {
        eventCount++;
        totalAmount = totalAmount.add(amount == null ? BigDecimal.ZERO : amount);
        updatedAt = now;
    }
}

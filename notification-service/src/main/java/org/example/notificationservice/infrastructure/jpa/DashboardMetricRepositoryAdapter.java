package org.example.notificationservice.infrastructure.jpa;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.application.repository.DashboardMetricRepository;
import org.example.notificationservice.domain.dashboard.DashboardMetric;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DashboardMetricRepositoryAdapter implements DashboardMetricRepository {
    private final JpaDashboardMetricRepository repository;

    @Override
    public void increment(String metricKey, BigDecimal amount, Instant updatedAt) {
        repository.increment(metricKey, amount, updatedAt);
    }

    @Override
    public Optional<DashboardMetric> findById(String metricKey) {
        return repository.findById(metricKey);
    }

    @Override
    public List<DashboardMetric> findAll() {
        return repository.findAll();
    }
}

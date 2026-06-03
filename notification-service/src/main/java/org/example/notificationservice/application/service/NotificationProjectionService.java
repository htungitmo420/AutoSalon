package org.example.notificationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.application.port.InboxEventProcessor;
import org.example.notificationservice.application.repository.DashboardMetricRepository;
import org.example.notificationservice.application.repository.NotificationRepository;
import org.example.notificationservice.application.repository.ReferenceOwnerRepository;
import org.example.notificationservice.domain.notification.NotificationType;
import org.example.notificationservice.domain.notification.UserNotification;
import org.example.notificationservice.domain.projection.ReferenceOwner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationProjectionService {
    private final NotificationRepository notificationRepository;
    private final DashboardMetricRepository dashboardMetricRepository;
    private final ReferenceOwnerRepository referenceOwnerRepository;
    private final InboxEventProcessor inboxEventProcessor;

    @Transactional
    public void project(UUID eventId, String topic, UUID referenceId, UUID customerId, String referenceType,
                        NotificationType type, String title, String message, String traceId, Instant occurredAt,
                        BigDecimal amount) {
        inboxEventProcessor.processOnce(eventId, topic, referenceId.toString(), traceId, () -> {
            Instant now = Instant.now();
            UUID resolvedCustomerId = customerId == null ? findOwner(referenceId) : customerId;
            if (customerId != null) {
                upsertOwner(referenceId, customerId, referenceType, now);
            }
            if (resolvedCustomerId != null) {
                notificationRepository.save(UserNotification.builder()
                        .customerId(resolvedCustomerId)
                        .type(type)
                        .referenceId(referenceId)
                        .title(title)
                        .message(message)
                        .traceId(traceId)
                        .occurredAt(occurredAt == null ? now : occurredAt)
                        .createdAt(now)
                        .build());
            }
            incrementMetric(topic, amount, now);
        });
    }

    private UUID findOwner(UUID referenceId) {
        return referenceOwnerRepository.findById(referenceId)
                .map(ReferenceOwner::getCustomerId)
                .orElse(null);
    }

    private void upsertOwner(UUID referenceId, UUID customerId, String referenceType, Instant now) {
        ReferenceOwner owner = ReferenceOwner.builder()
                .referenceId(referenceId)
                .build();
        owner.update(customerId, referenceType, now);
        referenceOwnerRepository.upsert(owner);
    }

    private void incrementMetric(String topic, BigDecimal amount, Instant now) {
        dashboardMetricRepository.increment(topic, amount == null ? BigDecimal.ZERO : amount, now);
    }
}

package org.example.notificationservice.application.dto;

import org.example.notificationservice.domain.notification.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        UUID referenceId,
        String title,
        String message,
        String traceId,
        Instant occurredAt,
        Instant readAt
) {
}

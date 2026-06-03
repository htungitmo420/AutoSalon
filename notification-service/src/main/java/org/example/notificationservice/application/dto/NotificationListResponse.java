package org.example.notificationservice.application.dto;

import java.util.List;

public record NotificationListResponse(
        long unreadCount,
        List<NotificationResponse> items
) {
}

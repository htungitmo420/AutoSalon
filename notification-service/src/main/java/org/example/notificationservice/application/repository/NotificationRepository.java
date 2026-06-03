package org.example.notificationservice.application.repository;

import org.example.notificationservice.domain.notification.UserNotification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    UserNotification save(UserNotification notification);
    List<UserNotification> findLatest(UUID customerId, boolean unreadOnly);
    Optional<UserNotification> findOwned(UUID notificationId, UUID customerId);
    long countUnread(UUID customerId);
}

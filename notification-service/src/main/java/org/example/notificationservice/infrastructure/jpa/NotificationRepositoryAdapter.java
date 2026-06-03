package org.example.notificationservice.infrastructure.jpa;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.application.repository.NotificationRepository;
import org.example.notificationservice.domain.notification.UserNotification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {
    private final JpaNotificationRepository repository;

    @Override
    public UserNotification save(UserNotification notification) {
        return repository.save(notification);
    }

    @Override
    public List<UserNotification> findLatest(UUID customerId, boolean unreadOnly) {
        return unreadOnly
                ? repository.findTop100ByCustomerIdAndReadAtIsNullOrderByOccurredAtDesc(customerId)
                : repository.findTop100ByCustomerIdOrderByOccurredAtDesc(customerId);
    }

    @Override
    public Optional<UserNotification> findOwned(UUID notificationId, UUID customerId) {
        return repository.findByIdAndCustomerId(notificationId, customerId);
    }

    @Override
    public long countUnread(UUID customerId) {
        return repository.countByCustomerIdAndReadAtIsNull(customerId);
    }
}

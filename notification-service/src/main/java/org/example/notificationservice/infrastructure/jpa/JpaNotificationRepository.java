package org.example.notificationservice.infrastructure.jpa;

import org.example.notificationservice.domain.notification.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<UserNotification, UUID> {
    List<UserNotification> findTop100ByCustomerIdOrderByOccurredAtDesc(UUID customerId);
    List<UserNotification> findTop100ByCustomerIdAndReadAtIsNullOrderByOccurredAtDesc(UUID customerId);
    Optional<UserNotification> findByIdAndCustomerId(UUID id, UUID customerId);
    long countByCustomerIdAndReadAtIsNull(UUID customerId);
}

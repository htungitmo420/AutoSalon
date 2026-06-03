package org.example.notificationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.application.dto.DashboardMetricResponse;
import org.example.notificationservice.application.dto.NotificationListResponse;
import org.example.notificationservice.application.dto.NotificationResponse;
import org.example.notificationservice.application.mapper.NotificationMapper;
import org.example.notificationservice.application.port.CurrentUserProvider;
import org.example.notificationservice.application.repository.DashboardMetricRepository;
import org.example.notificationservice.application.repository.NotificationRepository;
import org.example.notificationservice.domain.exception.EntityNotFoundException;
import org.example.notificationservice.domain.notification.UserNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {
    private final NotificationRepository notificationRepository;
    private final DashboardMetricRepository dashboardMetricRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationMapper mapper;

    @Transactional(readOnly = true)
    public NotificationListResponse list(boolean unreadOnly) {
        UUID customerId = currentUserProvider.getCurrentUserId();
        List<NotificationResponse> items = notificationRepository.findLatest(customerId, unreadOnly).stream()
                .map(mapper::toResponse)
                .toList();
        return new NotificationListResponse(notificationRepository.countUnread(customerId), items);
    }

    @Transactional
    public NotificationResponse markRead(UUID notificationId) {
        UUID customerId = currentUserProvider.getCurrentUserId();
        UserNotification notification = notificationRepository.findOwned(notificationId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + notificationId));
        notification.markRead(Instant.now());
        return mapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public List<DashboardMetricResponse> dashboard() {
        return dashboardMetricRepository.findAll().stream()
                .sorted(Comparator.comparing(metric -> metric.getMetricKey()))
                .map(mapper::toResponse)
                .toList();
    }
}

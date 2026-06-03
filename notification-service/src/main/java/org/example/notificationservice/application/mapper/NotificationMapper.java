package org.example.notificationservice.application.mapper;

import org.example.notificationservice.application.dto.DashboardMetricResponse;
import org.example.notificationservice.application.dto.NotificationResponse;
import org.example.notificationservice.domain.dashboard.DashboardMetric;
import org.example.notificationservice.domain.notification.UserNotification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(UserNotification notification);
    DashboardMetricResponse toResponse(DashboardMetric metric);
}

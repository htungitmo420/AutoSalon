package org.example.notificationservice.presentation;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.application.dto.DashboardMetricResponse;
import org.example.notificationservice.application.dto.NotificationListResponse;
import org.example.notificationservice.application.dto.NotificationResponse;
import org.example.notificationservice.application.service.NotificationQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class NotificationController {
    private final NotificationQueryService queryService;

    @GetMapping("/notifications")
    public NotificationListResponse list(@RequestParam(defaultValue = "false") boolean unreadOnly) {
        return queryService.list(unreadOnly);
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public NotificationResponse markRead(@PathVariable UUID notificationId) {
        return queryService.markRead(notificationId);
    }

    @GetMapping("/admin/dashboard/metrics")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<DashboardMetricResponse> dashboard() {
        return queryService.dashboard();
    }
}

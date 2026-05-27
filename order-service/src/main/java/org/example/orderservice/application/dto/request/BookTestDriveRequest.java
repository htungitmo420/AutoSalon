package org.example.orderservice.application.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookTestDriveRequest(
        UUID carId,
        UUID customerId,
        LocalDateTime startDateTime
) {}
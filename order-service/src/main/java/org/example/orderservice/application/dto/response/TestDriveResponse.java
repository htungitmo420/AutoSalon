package org.example.orderservice.application.dto.response;

import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestDriveResponse(
        UUID id,
        UUID carId,
        UUID customerId,
        TestDriveStatus status,
        LocalDateTime startDateTime
) {}
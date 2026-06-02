package org.example.orderservice.infrastructure.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.commoncontracts.grpc.reservation.InventoryReservationServiceGrpc;
import org.example.orderservice.application.client.InventoryReservationClient;
import org.example.orderservice.application.dto.response.InventoryReservationResponse;
import org.example.orderservice.application.exception.StorageServiceUnavailableException;
import org.example.orderservice.domain.exceptions.DomainValidationException;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;
import org.example.orderservice.infrastructure.logging.TraceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class StorageReservationGrpcClient implements InventoryReservationClient {

    @GrpcClient("storage-service")
    private InventoryReservationServiceGrpc.InventoryReservationServiceBlockingStub storageStub;

    @Value("${storage.grpc.timeout-millis:2000}")
    private long timeoutMillis;

    @Override
    public InventoryReservationResponse reserveStockCar(UUID orderId, UUID carId, Instant expiresAt) {
        try {
            return StorageReservationGrpcMapper.toResponse(storageStub.withDeadlineAfter(timeoutMillis, TimeUnit.MILLISECONDS)
                    .reserveStockCar(StorageReservationGrpcMapper.toReserveStockCarRequest(
                            orderId, carId, expiresAt, TraceContext.currentTraceId())));
        } catch (StatusRuntimeException ex) {
            throw mapException(ex, "Failed to reserve stock car");
        }
    }

    @Override
    public InventoryReservationResponse reserveConfiguration(UUID orderId, UUID modelId,
                                                             Map<String, UUID> selectedPartIds, Instant expiresAt) {
        try {
            return StorageReservationGrpcMapper.toResponse(storageStub.withDeadlineAfter(timeoutMillis, TimeUnit.MILLISECONDS)
                    .reserveConfiguration(StorageReservationGrpcMapper.toReserveConfigurationRequest(
                            orderId, modelId, selectedPartIds, expiresAt, TraceContext.currentTraceId())));
        } catch (StatusRuntimeException ex) {
            throw mapException(ex, "Failed to reserve configured car");
        }
    }

    @Override
    public InventoryReservationResponse confirmReservation(UUID orderId, UUID reservationId) {
        try {
            return StorageReservationGrpcMapper.toResponse(storageStub.withDeadlineAfter(timeoutMillis, TimeUnit.MILLISECONDS)
                    .confirmReservation(StorageReservationGrpcMapper.toConfirmReservationRequest(
                            orderId, reservationId, TraceContext.currentTraceId())));
        } catch (StatusRuntimeException ex) {
            throw mapException(ex, "Failed to confirm reservation");
        }
    }

    @Override
    public InventoryReservationResponse releaseReservation(UUID orderId, UUID reservationId, String reason) {
        try {
            return StorageReservationGrpcMapper.toResponse(storageStub.withDeadlineAfter(timeoutMillis, TimeUnit.MILLISECONDS)
                    .releaseReservation(StorageReservationGrpcMapper.toReleaseReservationRequest(
                            orderId, reservationId, reason, TraceContext.currentTraceId())));
        } catch (StatusRuntimeException ex) {
            throw mapException(ex, "Failed to release reservation");
        }
    }

    private RuntimeException mapException(StatusRuntimeException ex, String fallbackMessage) {
        Status status = ex.getStatus();
        String description = status.getDescription() == null ? fallbackMessage : status.getDescription();
        return switch (status.getCode()) {
            case NOT_FOUND -> new EntityNotFoundException(description);
            case INVALID_ARGUMENT -> new IllegalArgumentException(description);
            case FAILED_PRECONDITION, ALREADY_EXISTS -> new DomainValidationException(description);
            case DEADLINE_EXCEEDED, UNAVAILABLE -> new StorageServiceUnavailableException(description, ex);
            default -> new StorageServiceUnavailableException(fallbackMessage, ex);
        };
    }
}

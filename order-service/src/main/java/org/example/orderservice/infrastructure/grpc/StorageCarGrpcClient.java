package org.example.orderservice.infrastructure.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.commoncontracts.grpc.car.CarInventoryServiceGrpc;
import org.example.commoncontracts.grpc.car.GetAvailableCarByIdRequest;
import org.example.commoncontracts.grpc.car.GetAvailableCarByIdResponse;
import org.example.commoncontracts.grpc.car.GetAvailableCarsRequest;
import org.example.commoncontracts.grpc.car.GetAvailableCarsResponse;
import org.example.orderservice.application.client.AvailableCarClient;
import org.example.orderservice.application.dto.response.AvailableCarResponse;
import org.example.orderservice.application.exception.StorageServiceUnavailableException;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class StorageCarGrpcClient implements AvailableCarClient {

    @GrpcClient("storage-service")
    private CarInventoryServiceGrpc.CarInventoryServiceBlockingStub storageStub;

    @Value("${storage.grpc.timeout-millis:2000}")
    private long timeoutMillis;

    @Override
    public List<AvailableCarResponse> listAvailableCars() {
        log.info("Requesting available cars from StorageService by gRPC");

        try {
            GetAvailableCarsResponse response = storageStub
                    .withDeadlineAfter(timeoutMillis, TimeUnit.MILLISECONDS)
                    .getAvailableCars(GetAvailableCarsRequest.newBuilder().build());

            log.info("Received available cars from StorageService by gRPC, count={}", response.getCarsCount());
            return StorageCarGrpcMapper.toAvailableCarResponses(response);
        } catch (StatusRuntimeException ex) {
            throw mapGrpcException(ex, "Failed to fetch available cars from StorageService");
        }
    }

    @Override
    public AvailableCarResponse getAvailableCar(UUID carId) {
        log.info("Requesting available car from StorageService by gRPC, carId={}", carId);

        try {
            GetAvailableCarByIdResponse response = storageStub
                    .withDeadlineAfter(timeoutMillis, TimeUnit.MILLISECONDS)
                    .getAvailableCarById(GetAvailableCarByIdRequest.newBuilder()
                            .setId(carId.toString())
                            .build());

            log.info("Received available car from StorageService by gRPC, carId={}", carId);
            return StorageCarGrpcMapper.toAvailableCarResponse(response.getCar());
        } catch (StatusRuntimeException ex) {
            throw mapGrpcException(ex, "Failed to fetch available car from StorageService");
        }
    }

    private RuntimeException mapGrpcException(StatusRuntimeException ex, String fallbackMessage) {
        Status status = ex.getStatus();
        String description = status.getDescription() == null ? fallbackMessage : status.getDescription();

        return switch (status.getCode()) {
            case NOT_FOUND -> new EntityNotFoundException(description);
            case INVALID_ARGUMENT -> new IllegalArgumentException(description);
            case DEADLINE_EXCEEDED, UNAVAILABLE -> new StorageServiceUnavailableException(description, ex);
            default -> new StorageServiceUnavailableException(fallbackMessage, ex);
        };
    }
}

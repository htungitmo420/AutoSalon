package org.example.storageservice.infrastructure.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.commoncontracts.grpc.reservation.ConfirmReservationRequest;
import org.example.commoncontracts.grpc.reservation.InventoryReservationServiceGrpc;
import org.example.commoncontracts.grpc.reservation.ReleaseReservationRequest;
import org.example.commoncontracts.grpc.reservation.ReservationResponse;
import org.example.commoncontracts.grpc.reservation.ReserveConfigurationRequest;
import org.example.commoncontracts.grpc.reservation.ReserveStockCarRequest;
import org.example.storageservice.application.service.InventoryReservationService;
import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class InventoryReservationGrpcService
        extends InventoryReservationServiceGrpc.InventoryReservationServiceImplBase {

    private final InventoryReservationService inventoryReservationService;

    @Override
    public void reserveStockCar(ReserveStockCarRequest request, StreamObserver<ReservationResponse> observer) {
        execute(observer, () -> inventoryReservationService.reserveStockCar(
                UUID.fromString(request.getOrderId()),
                UUID.fromString(request.getCarId()),
                Instant.parse(request.getExpiresAt()),
                request.getTraceId()));
    }

    @Override
    public void reserveConfiguration(ReserveConfigurationRequest request, StreamObserver<ReservationResponse> observer) {
        execute(observer, () -> inventoryReservationService.reserveConfiguration(
                UUID.fromString(request.getOrderId()),
                UUID.fromString(request.getModelId()),
                parsePartIds(request.getSelectedPartIdsMap()),
                Instant.parse(request.getExpiresAt()),
                request.getTraceId()));
    }

    @Override
    public void confirmReservation(ConfirmReservationRequest request, StreamObserver<ReservationResponse> observer) {
        execute(observer, () -> inventoryReservationService.confirmReservation(
                UUID.fromString(request.getOrderId()),
                UUID.fromString(request.getReservationId()),
                request.getTraceId()));
    }

    @Override
    public void releaseReservation(ReleaseReservationRequest request, StreamObserver<ReservationResponse> observer) {
        execute(observer, () -> inventoryReservationService.releaseReservation(
                UUID.fromString(request.getOrderId()),
                UUID.fromString(request.getReservationId()),
                request.getReason(),
                request.getTraceId()));
    }

    private Map<String, UUID> parsePartIds(Map<String, String> partIds) {
        Map<String, UUID> result = new HashMap<>();
        partIds.forEach((slot, id) -> result.put(slot, UUID.fromString(id)));
        return result;
    }

    private void execute(StreamObserver<ReservationResponse> observer, ReservationOperation operation) {
        try {
            ReservationResponse response = InventoryReservationGrpcMapper.toResponse(operation.execute());
            observer.onNext(response);
            observer.onCompleted();
        } catch (EntityNotFoundException ex) {
            observer.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        } catch (DomainValidationException ex) {
            observer.onError(Status.FAILED_PRECONDITION.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        } catch (IllegalArgumentException ex) {
            observer.onError(Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
        } catch (RuntimeException ex) {
            log.error("Inventory reservation gRPC operation failed", ex);
            observer.onError(Status.INTERNAL.withDescription("Inventory reservation operation failed")
                    .withCause(ex).asRuntimeException());
        }
    }

    @FunctionalInterface
    private interface ReservationOperation {
        org.example.storageservice.domain.reservation.model.InventoryReservation execute();
    }
}

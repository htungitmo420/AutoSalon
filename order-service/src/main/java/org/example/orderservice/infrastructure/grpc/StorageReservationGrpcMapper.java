package org.example.orderservice.infrastructure.grpc;

import org.example.commoncontracts.grpc.reservation.ConfirmReservationRequest;
import org.example.commoncontracts.grpc.reservation.ReservationResponse;
import org.example.commoncontracts.grpc.reservation.ReserveConfigurationRequest;
import org.example.commoncontracts.grpc.reservation.ReserveStockCarRequest;
import org.example.orderservice.application.dto.response.InventoryReservationResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class StorageReservationGrpcMapper {

    private StorageReservationGrpcMapper() {
    }

    public static ReserveStockCarRequest toReserveStockCarRequest(
            UUID orderId, UUID carId, Instant expiresAt, String traceId) {
        return ReserveStockCarRequest.newBuilder()
                .setOrderId(orderId.toString())
                .setCarId(carId.toString())
                .setExpiresAt(expiresAt.toString())
                .setTraceId(traceId)
                .build();
    }

    public static ReserveConfigurationRequest toReserveConfigurationRequest(
            UUID orderId, UUID modelId, Map<String, UUID> selectedPartIds, Instant expiresAt, String traceId) {
        Map<String, String> partIds = selectedPartIds.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
        return ReserveConfigurationRequest.newBuilder()
                .setOrderId(orderId.toString())
                .setModelId(modelId.toString())
                .putAllSelectedPartIds(partIds)
                .setExpiresAt(expiresAt.toString())
                .setTraceId(traceId)
                .build();
    }

    public static ConfirmReservationRequest toConfirmReservationRequest(
            UUID orderId, UUID reservationId, String traceId) {
        return ConfirmReservationRequest.newBuilder()
                .setOrderId(orderId.toString())
                .setReservationId(reservationId.toString())
                .setTraceId(traceId)
                .build();
    }

    public static InventoryReservationResponse toResponse(ReservationResponse response) {
        BigDecimal totalPrice = response.getTotalPrice().isBlank()
                ? null
                : new BigDecimal(response.getTotalPrice());
        return new InventoryReservationResponse(
                UUID.fromString(response.getReservationId()),
                response.getStatus(),
                Instant.parse(response.getExpiresAt()),
                totalPrice);
    }
}

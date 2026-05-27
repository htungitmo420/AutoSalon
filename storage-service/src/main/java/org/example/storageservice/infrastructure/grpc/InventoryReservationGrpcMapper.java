package org.example.storageservice.infrastructure.grpc;

import org.example.commoncontracts.grpc.reservation.ReservationResponse;
import org.example.storageservice.domain.reservation.model.InventoryReservation;

public final class InventoryReservationGrpcMapper {

    private InventoryReservationGrpcMapper() {
    }

    public static ReservationResponse toResponse(InventoryReservation reservation) {
        ReservationResponse.Builder response = ReservationResponse.newBuilder()
                .setReservationId(reservation.getId().toString())
                .setOrderId(reservation.getOrderId().toString())
                .setOrderType(reservation.getSourceOrderType().name())
                .setStatus(reservation.getStatus().name())
                .setExpiresAt(reservation.getExpiresAt().toString());
        if (reservation.getTotalPrice() != null) {
            response.setTotalPrice(reservation.getTotalPrice().toPlainString());
        }
        return response.build();
    }
}

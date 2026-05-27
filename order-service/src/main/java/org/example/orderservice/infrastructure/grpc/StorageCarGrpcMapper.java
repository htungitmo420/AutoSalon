package org.example.orderservice.infrastructure.grpc;

import org.example.commoncontracts.grpc.car.AvailableCar;
import org.example.commoncontracts.grpc.car.GetAvailableCarsResponse;
import org.example.orderservice.application.dto.response.AvailableCarResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class StorageCarGrpcMapper {

    private StorageCarGrpcMapper() {
    }

    public static List<AvailableCarResponse> toAvailableCarResponses(GetAvailableCarsResponse response) {
        return response.getCarsList().stream()
                .map(StorageCarGrpcMapper::toAvailableCarResponse)
                .toList();
    }

    public static AvailableCarResponse toAvailableCarResponse(AvailableCar car) {
        return new AvailableCarResponse(
                UUID.fromString(car.getId()),
                UUID.fromString(car.getModelId()),
                car.getModelName(),
                car.getBrand(),
                car.getBodyType(),
                car.getFuelType(),
                car.getColor(),
                new BigDecimal(car.getPrice()));
    }
}

package org.example.storageservice.infrastructure.grpc;

import org.example.commoncontracts.grpc.car.AvailableCar;
import org.example.commoncontracts.grpc.car.GetAvailableCarsResponse;
import org.example.storageservice.application.dto.response.AvailableCarResponse;

import java.util.List;

public final class CarInventoryGrpcMapper {

    private CarInventoryGrpcMapper() {
    }

    public static GetAvailableCarsResponse toGetAvailableCarsResponse(List<AvailableCarResponse> cars) {
        return GetAvailableCarsResponse.newBuilder()
                .addAllCars(cars.stream()
                        .map(CarInventoryGrpcMapper::toAvailableCar)
                        .toList())
                .build();
    }

    public static AvailableCar toAvailableCar(AvailableCarResponse car) {
        return AvailableCar.newBuilder()
                .setId(car.id().toString())
                .setModelId(car.modelId().toString())
                .setModelName(car.modelName())
                .setBrand(car.brand().name())
                .setBodyType(car.bodyType().name())
                .setFuelType(car.fuelType().name())
                .setColor(car.color().name())
                .setPrice(car.price().toPlainString())
                .build();
    }
}
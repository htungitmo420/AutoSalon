package org.example.storageservice.infrastructure.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.commoncontracts.grpc.car.CarInventoryServiceGrpc;
import org.example.commoncontracts.grpc.car.GetAvailableCarByIdRequest;
import org.example.commoncontracts.grpc.car.GetAvailableCarByIdResponse;
import org.example.commoncontracts.grpc.car.GetAvailableCarsRequest;
import org.example.commoncontracts.grpc.car.GetAvailableCarsResponse;
import org.example.storageservice.application.dto.response.AvailableCarResponse;
import org.example.storageservice.application.service.AvailableCarService;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CarInventoryGrpcService extends CarInventoryServiceGrpc.CarInventoryServiceImplBase {

    private final AvailableCarService availableCarService;

    @Override
    public void getAvailableCars(GetAvailableCarsRequest request,
                                 StreamObserver<GetAvailableCarsResponse> responseObserver) {
        log.info("Received gRPC request to list available cars");

        try {
            GetAvailableCarsResponse response = CarInventoryGrpcMapper
                    .toGetAvailableCarsResponse(availableCarService.listAvailableCars());

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("Finished gRPC request to list available cars, count={}", response.getCarsCount());
        } catch (RuntimeException e) {
            log.error("Failed to list available cars by gRPC", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to list available cars")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getAvailableCarById(GetAvailableCarByIdRequest request,
                                    StreamObserver<GetAvailableCarByIdResponse> responseObserver) {
        log.info("Received gRPC request to get available car, carId={}", request.getId());

        try {
            UUID carId = UUID.fromString(request.getId());
            AvailableCarResponse car = availableCarService.getAvailableCar(carId);

            GetAvailableCarByIdResponse response = GetAvailableCarByIdResponse.newBuilder()
                    .setCar(CarInventoryGrpcMapper.toAvailableCar(car))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("Finished gRPC request to get available car, carId={}", carId);
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid car id: " + request.getId())
                    .withCause(e)
                    .asRuntimeException());
        } catch (EntityNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        } catch (RuntimeException e) {
            log.error("Failed to get available car by gRPC, carId={}", request.getId(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to get available car")
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}

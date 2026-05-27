package org.example.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.commoncontracts.grpc.car.AvailableCar;
import org.example.commoncontracts.grpc.car.CarInventoryServiceGrpc;
import org.example.commoncontracts.grpc.car.GetAvailableCarByIdRequest;
import org.example.commoncontracts.grpc.car.GetAvailableCarByIdResponse;
import org.example.commoncontracts.grpc.car.GetAvailableCarsRequest;
import org.example.commoncontracts.grpc.car.GetAvailableCarsResponse;
import org.example.orderservice.application.dto.response.AvailableCarResponse;
import org.example.orderservice.application.exception.StorageServiceUnavailableException;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;
import org.example.orderservice.infrastructure.grpc.StorageCarGrpcClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageCarGrpcClientIT {

    private static final String LOCALHOST = "localhost";
    private static final long DEFAULT_TIMEOUT_MILLIS = 500;
    private static final long SHORT_TIMEOUT_MILLIS = 50;
    private static final long SLOW_RESPONSE_MILLIS = 300;

    private Server server;
    private ManagedChannel channel;
    private StorageCarGrpcClient client;

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    void listAvailableCars_ReturnsMappedCars() throws Exception {
        AvailableCar grpcCar = availableCar(UUID.randomUUID(), UUID.randomUUID(), "X5", "BMW");
        startServer(new CarInventoryServiceGrpc.CarInventoryServiceImplBase() {
            @Override
            public void getAvailableCars(GetAvailableCarsRequest request,
                                         StreamObserver<GetAvailableCarsResponse> responseObserver) {
                responseObserver.onNext(GetAvailableCarsResponse.newBuilder()
                        .addCars(grpcCar)
                        .build());

                responseObserver.onCompleted();
            }
        }, DEFAULT_TIMEOUT_MILLIS);

        List<AvailableCarResponse> result = client.listAvailableCars();

        assertEquals(1, result.size());
        assertEquals(UUID.fromString(grpcCar.getId()), result.getFirst().id());
        assertEquals("X5", result.getFirst().modelName());
        assertEquals("BMW", result.getFirst().brand());
        assertEquals(BigDecimal.valueOf(2500000), result.getFirst().price());
    }

    @Test
    void listAvailableCars_ReturnsEmptyList() throws Exception {
        startServer(new CarInventoryServiceGrpc.CarInventoryServiceImplBase() {
            @Override
            public void getAvailableCars(GetAvailableCarsRequest request,
                                         StreamObserver<GetAvailableCarsResponse> responseObserver) {
                responseObserver.onNext(GetAvailableCarsResponse.newBuilder().build());
                responseObserver.onCompleted();
            }
        }, DEFAULT_TIMEOUT_MILLIS);

        List<AvailableCarResponse> result = client.listAvailableCars();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailableCar_ReturnsMappedCar() throws Exception {
        UUID carId = UUID.randomUUID();
        AvailableCar grpcCar = availableCar(carId, UUID.randomUUID(), "Camry", "TOYOTA");
        startServer(new CarInventoryServiceGrpc.CarInventoryServiceImplBase() {
            @Override
            public void getAvailableCarById(GetAvailableCarByIdRequest request,
                                            StreamObserver<GetAvailableCarByIdResponse> responseObserver) {
                responseObserver.onNext(GetAvailableCarByIdResponse.newBuilder()
                        .setCar(grpcCar)
                        .build());
                responseObserver.onCompleted();
            }
        }, DEFAULT_TIMEOUT_MILLIS);

        AvailableCarResponse result = client.getAvailableCar(carId);

        assertEquals(carId, result.id());
        assertEquals("Camry", result.modelName());
        assertEquals("TOYOTA", result.brand());
    }

    @Test
    void getAvailableCar_MapsNotFoundToEntityNotFoundException() throws Exception {
        startServer(new CarInventoryServiceGrpc.CarInventoryServiceImplBase() {
            @Override
            public void getAvailableCarById(GetAvailableCarByIdRequest request,
                                            StreamObserver<GetAvailableCarByIdResponse> responseObserver) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Available car not found: " + request.getId())
                        .asRuntimeException());
            }
        }, DEFAULT_TIMEOUT_MILLIS);

        assertThrows(EntityNotFoundException.class, () -> client.getAvailableCar(UUID.randomUUID()));
    }

    @Test
    void listAvailableCars_MapsTimeoutToServiceUnavailableException() throws Exception {
        startServer(new CarInventoryServiceGrpc.CarInventoryServiceImplBase() {
            @Override
            public void getAvailableCars(GetAvailableCarsRequest request,
                                         StreamObserver<GetAvailableCarsResponse> responseObserver) {
                sleep(SLOW_RESPONSE_MILLIS);
                responseObserver.onNext(GetAvailableCarsResponse.newBuilder().build());
                responseObserver.onCompleted();
            }
        }, SHORT_TIMEOUT_MILLIS);

        assertThrows(StorageServiceUnavailableException.class, () -> client.listAvailableCars());
    }

    @Test
    void listAvailableCars_MapsUnavailableToServiceUnavailableException() throws Exception {
        int unusedPort = findUnusedPort();
        channel = ManagedChannelBuilder.forAddress(LOCALHOST, unusedPort)
                .usePlaintext()
                .build();
        client = clientWithStub(CarInventoryServiceGrpc.newBlockingStub(channel), SHORT_TIMEOUT_MILLIS);

        assertThrows(StorageServiceUnavailableException.class, () -> client.listAvailableCars());
    }

    private void startServer(CarInventoryServiceGrpc.CarInventoryServiceImplBase service,
                             long timeoutMillis) throws IOException {
        server = ServerBuilder.forPort(0)
                .addService(service)
                .build()
                .start();
        channel = ManagedChannelBuilder.forAddress(LOCALHOST, server.getPort())
                .usePlaintext()
                .build();
        client = clientWithStub(CarInventoryServiceGrpc.newBlockingStub(channel), timeoutMillis);
    }

    private StorageCarGrpcClient clientWithStub(CarInventoryServiceGrpc.CarInventoryServiceBlockingStub stub,
                                                long timeoutMillis) {
        StorageCarGrpcClient grpcClient = new StorageCarGrpcClient();
        ReflectionTestUtils.setField(grpcClient, "storageStub", stub);
        ReflectionTestUtils.setField(grpcClient, "timeoutMillis", timeoutMillis);
        return grpcClient;
    }

    private AvailableCar availableCar(UUID carId, UUID modelId, String modelName, String brand) {
        return AvailableCar.newBuilder()
                .setId(carId.toString())
                .setModelId(modelId.toString())
                .setModelName(modelName)
                .setBrand(brand)
                .setBodyType("SUV")
                .setFuelType("GASOLINE")
                .setColor("BLACK")
                .setPrice(BigDecimal.valueOf(2500000).toPlainString())
                .build();
    }

    private int findUnusedPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}

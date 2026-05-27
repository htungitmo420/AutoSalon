package org.example.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;
import org.example.commoncontracts.grpc.car.CarInventoryServiceGrpc;
import org.example.commoncontracts.grpc.car.GetAvailableCarByIdResponse;
import org.example.commoncontracts.grpc.car.GetAvailableCarByIdRequest;
import org.example.commoncontracts.grpc.car.GetAvailableCarsRequest;
import org.example.commoncontracts.grpc.car.GetAvailableCarsResponse;
import org.example.commoncontracts.grpc.reservation.InventoryReservationServiceGrpc;
import org.example.commoncontracts.grpc.reservation.ConfirmReservationRequest;
import org.example.commoncontracts.grpc.reservation.ReleaseReservationRequest;
import org.example.commoncontracts.grpc.reservation.ReservationResponse;
import org.example.commoncontracts.grpc.reservation.ReserveConfigurationRequest;
import org.example.commoncontracts.grpc.reservation.ReserveStockCarRequest;
import org.example.storageservice.StorageServiceApplication;
import org.example.storageservice.application.dto.request.SaveAssemblyOrderRequest;
import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.request.SaveCarRequest;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.request.SavePartStockRequest;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.dto.response.PartStockResponse;
import org.example.storageservice.application.repository.InventoryReservationRepository;
import org.example.storageservice.application.repository.CarRepository;
import org.example.storageservice.application.service.AssemblyOrderService;
import org.example.storageservice.application.service.CarModelService;
import org.example.storageservice.application.service.CarService;
import org.example.storageservice.application.service.InventoryReservationService;
import org.example.storageservice.application.service.PartService;
import org.example.storageservice.application.service.PartStockService;
import org.example.storageservice.domain.assembly.enums.AssemblyOrderStatus;
import org.example.storageservice.domain.assembly.enums.SourceOrderType;
import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.Color;
import org.example.storageservice.domain.car.enums.DrivetrainType;
import org.example.storageservice.domain.car.enums.FuelType;
import org.example.storageservice.domain.car.enums.GearBoxType;
import org.example.storageservice.domain.car.model.Car;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.domain.reservation.enums.ReservationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = StorageServiceApplication.class)
@ActiveProfiles("integrationtest")
@Testcontainers
@Import(CarInventoryGrpcServiceIT.GrpcTestConfig.class)
class CarInventoryGrpcServiceIT {

    private static final String LOCALHOST = "localhost";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("storage_service")
            .withUsername("postgres")
            .withPassword("123456");

    private ManagedChannel channel;
    private CarInventoryServiceGrpc.CarInventoryServiceBlockingStub stub;
    private InventoryReservationServiceGrpc.InventoryReservationServiceBlockingStub reservationStub;

    @Autowired
    private GrpcServerPortHolder grpcServerPortHolder;

    @Autowired
    private CarModelService carModelService;

    @Autowired
    private CarService carService;

    @Autowired
    private AssemblyOrderService assemblyOrderService;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private PartService partService;

    @Autowired
    private PartStockService partStockService;

    @Autowired
    private InventoryReservationService inventoryReservationService;

    @Autowired
    private InventoryReservationRepository inventoryReservationRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUpGrpcClient() {
        channel = ManagedChannelBuilder.forAddress(LOCALHOST, grpcServerPortHolder.port())
                .usePlaintext()
                .build();
        stub = CarInventoryServiceGrpc.newBlockingStub(channel);
        reservationStub = InventoryReservationServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDownGrpcClient() {
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @Test
    void getAvailableCars_ReturnsOnlyCarsAvailableForSale() {
        CarModelResponse model = createModel("grpc-available-list");
        CarResponse availableCar = createCar(model.id(), Color.BLACK);
        CarResponse testDriveCar = createCar(model.id(), Color.WHITE);
        CarResponse reservedCar = createCar(model.id(), Color.RED);

        markAsTestDrive(testDriveCar.id());
        reserveCar(reservedCar.id());

        GetAvailableCarsResponse response = stub.getAvailableCars(GetAvailableCarsRequest.newBuilder().build());

        assertTrue(response.getCarsList().stream()
                .anyMatch(car -> car.getId().equals(availableCar.id().toString())));
        assertFalse(response.getCarsList().stream()
                .anyMatch(car -> car.getId().equals(testDriveCar.id().toString())));
        assertFalse(response.getCarsList().stream()
                .anyMatch(car -> car.getId().equals(reservedCar.id().toString())));
    }

    @Test
    void getAvailableCarById_ReturnsAvailableCar() {
        CarModelResponse model = createModel("grpc-available-by-id");
        CarResponse car = createCar(model.id(), Color.BLUE);

        GetAvailableCarByIdResponse response = stub.getAvailableCarById(GetAvailableCarByIdRequest.newBuilder()
                .setId(car.id().toString())
                .build());

        assertEquals(car.id().toString(), response.getCar().getId());
        assertEquals(model.id().toString(), response.getCar().getModelId());
        assertEquals("grpc-available-by-id", response.getCar().getModelName());
        assertEquals("BLUE", response.getCar().getColor());
    }

    @Test
    void getAvailableCarById_ReturnsNotFoundForUnavailableCar() {
        CarModelResponse model = createModel("grpc-reserved-by-id");
        CarResponse reservedCar = createCar(model.id(), Color.GREEN);
        reserveCar(reservedCar.id());

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                stub.getAvailableCarById(GetAvailableCarByIdRequest.newBuilder()
                        .setId(reservedCar.id().toString())
                        .build()));

        assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
    }

    @Test
    void getAvailableCarById_ReturnsInvalidArgumentForBadUuid() {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                stub.getAvailableCarById(GetAvailableCarByIdRequest.newBuilder()
                        .setId("not-a-uuid")
                        .build()));

        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
    }

    @Test
    void reserveStockCar_HoldsInventoryAndRejectsASecondOrder() {
        CarModelResponse model = createModel("grpc-reservation");
        CarResponse car = createCar(model.id(), Color.BLACK);

        ReservationResponse reservation = reservationStub.reserveStockCar(ReserveStockCarRequest.newBuilder()
                .setOrderId(UUID.randomUUID().toString())
                .setCarId(car.id().toString())
                .setExpiresAt(Instant.now().plusSeconds(900).toString())
                .setTraceId("grpc-reservation-trace")
                .build());

        assertEquals("HELD", reservation.getStatus());

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                reservationStub.reserveStockCar(ReserveStockCarRequest.newBuilder()
                        .setOrderId(UUID.randomUUID().toString())
                        .setCarId(car.id().toString())
                        .setExpiresAt(Instant.now().plusSeconds(900).toString())
                        .setTraceId("grpc-reservation-conflict")
                        .build()));

        assertEquals(Status.Code.FAILED_PRECONDITION, ex.getStatus().getCode());
    }

    @Test
    void reserveStockCar_AllowsOnlyOneConcurrentReservation() throws Exception {
        CarResponse car = createCar(createModel("grpc-race").id(), Color.WHITE);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Status.Code> first = executor.submit(() -> concurrentReserveOutcome(car.id(), ready, start));
            Future<Status.Code> second = executor.submit(() -> concurrentReserveOutcome(car.id(), ready, start));

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            List<Status.Code> results = List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
            assertEquals(1, results.stream().filter(Status.Code.OK::equals).count());
            assertEquals(1, results.stream().filter(Status.Code.FAILED_PRECONDITION::equals).count());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void reserveConfiguration_CanBeConfirmedAndLeavesStockHeldForAssembly() {
        ConfiguredInventory inventory = createConfiguredInventory("grpc-custom-confirm", 2);
        UUID orderId = UUID.randomUUID();

        ReservationResponse held = reserveConfiguration(orderId, inventory);
        assertEquals("HELD", held.getStatus());
        assertEquals(1, partStockService.getPartStock(inventory.stockId()).reservedQuantity());

        ReservationResponse confirmed = reservationStub.confirmReservation(ConfirmReservationRequest.newBuilder()
                .setOrderId(orderId.toString())
                .setReservationId(held.getReservationId())
                .setTraceId("grpc-custom-confirm")
                .build());

        assertEquals("CONFIRMED", confirmed.getStatus());
        PartStockResponse stock = partStockService.getPartStock(inventory.stockId());
        assertEquals(2, stock.quantity());
        assertEquals(1, stock.reservedQuantity());
    }

    @Test
    void reserveConfiguration_CanBeReleasedAndReturnsHeldStock() {
        ConfiguredInventory inventory = createConfiguredInventory("grpc-custom-release", 1);
        UUID orderId = UUID.randomUUID();
        ReservationResponse held = reserveConfiguration(orderId, inventory);

        ReservationResponse released = reservationStub.releaseReservation(ReleaseReservationRequest.newBuilder()
                .setOrderId(orderId.toString())
                .setReservationId(held.getReservationId())
                .setReason("payment cancelled")
                .setTraceId("grpc-custom-release")
                .build());

        assertEquals("RELEASED", released.getStatus());
        assertEquals(0, partStockService.getPartStock(inventory.stockId()).reservedQuantity());
    }

    @Test
    void reserveConfiguration_RejectsASecondOrderWhenPartStockIsHeld() {
        ConfiguredInventory inventory = createConfiguredInventory("grpc-custom-exhausted", 1);
        reserveConfiguration(UUID.randomUUID(), inventory);

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> reserveConfiguration(UUID.randomUUID(), inventory));

        assertEquals(Status.Code.FAILED_PRECONDITION, ex.getStatus().getCode());
    }

    @Test
    void expireHeldReservation_ReleasesConfigurationStock() {
        ConfiguredInventory inventory = createConfiguredInventory("grpc-custom-expiration", 1);
        ReservationResponse held = reserveConfiguration(UUID.randomUUID(), inventory);
        UUID reservationId = UUID.fromString(held.getReservationId());
        var reservation = inventoryReservationRepository.findById(reservationId).orElseThrow();
        reservation.setExpiresAt(Instant.now().minusSeconds(1));
        inventoryReservationRepository.save(reservation);

        inventoryReservationService.expireHeldReservations();

        assertEquals(ReservationStatus.EXPIRED,
                inventoryReservationRepository.findById(reservationId).orElseThrow().getStatus());
        assertEquals(0, partStockService.getPartStock(inventory.stockId()).reservedQuantity());
    }

    private CarModelResponse createModel(String name) {
        return carModelService.createCarModel(new SaveCarModelRequest(
                name,
                Brand.BMW,
                BodyType.SEDAN,
                FuelType.GASOLINE,
                250,
                2.5,
                GearBoxType.AUTOMATIC,
                DrivetrainType.RWD,
                BigDecimal.valueOf(3000000),
                Map.of()
        ));
    }

    private CarResponse createCar(UUID modelId, Color color) {
        return carService.createCar(new SaveCarRequest(modelId, color, BigDecimal.valueOf(2500000)));
    }

    private void markAsTestDrive(UUID carId) {
        Car car = carRepository.findById(carId).orElseThrow();
        car.setTestDrive(true);
        carRepository.save(car);
    }

    private void reserveCar(UUID carId) {
        assemblyOrderService.createAssemblyOrder(new SaveAssemblyOrderRequest(
                UUID.randomUUID(),
                SourceOrderType.COMMON,
                carId,
                null,
                Map.of(),
                UUID.randomUUID(),
                AssemblyOrderStatus.CREATED,
                null
        ));
    }

    private Status.Code concurrentReserveOutcome(UUID carId, CountDownLatch ready, CountDownLatch start)
            throws InterruptedException {
        ready.countDown();
        assertTrue(start.await(5, TimeUnit.SECONDS));
        try {
            reservationStub.reserveStockCar(ReserveStockCarRequest.newBuilder()
                    .setOrderId(UUID.randomUUID().toString())
                    .setCarId(carId.toString())
                    .setExpiresAt(Instant.now().plusSeconds(900).toString())
                    .setTraceId("grpc-race")
                    .build());
            return Status.Code.OK;
        } catch (StatusRuntimeException ex) {
            return ex.getStatus().getCode();
        }
    }

    private ReservationResponse reserveConfiguration(UUID orderId, ConfiguredInventory inventory) {
        return reservationStub.reserveConfiguration(ReserveConfigurationRequest.newBuilder()
                .setOrderId(orderId.toString())
                .setModelId(inventory.modelId().toString())
                .putSelectedPartIds("WHEELS", inventory.partId().toString())
                .setExpiresAt(Instant.now().plusSeconds(900).toString())
                .setTraceId("grpc-configuration")
                .build());
    }

    private ConfiguredInventory createConfiguredInventory(String name, int quantity) {
        CarModelResponse model = createModel(name);
        PartResponse part = partService.createPart(new SavePartRequest(
                name + "-wheels", PartType.WHEELS, BigDecimal.valueOf(120000), Set.of(model.id())));
        model = carModelService.updateCarModel(model.id(), new SaveCarModelRequest(
                name, Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE, 250, 2.5,
                GearBoxType.AUTOMATIC, DrivetrainType.RWD, BigDecimal.valueOf(3000000),
                Map.of(PartType.WHEELS, part.id())));
        PartStockResponse stock = partStockService.createPartStock(new SavePartStockRequest(part.id(), quantity, 0));
        return new ConfiguredInventory(model.id(), part.id(), stock.id());
    }

    private record ConfiguredInventory(UUID modelId, UUID partId, UUID stockId) {
    }

    @TestConfiguration
    static class GrpcTestConfig {

        @Bean
        GrpcServerPortHolder grpcServerPortHolder() {
            return new GrpcServerPortHolder();
        }
    }

    static class GrpcServerPortHolder implements ApplicationListener<GrpcServerStartedEvent> {

        private int port;

        @Override
        public void onApplicationEvent(GrpcServerStartedEvent event) {
            this.port = event.getPort();
        }

        int port() {
            if (port <= 0) {
                throw new IllegalStateException("gRPC test server port was not initialized");
            }
            return port;
        }
    }
}

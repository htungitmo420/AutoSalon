package org.example.storageservice.application.service;

import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.request.SaveCarRequest;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.request.SavePartStockRequest;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.port.out.InventoryReservationEventPublisher;
import org.example.storageservice.application.repository.AssemblyOrderRepository;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.CarRepository;
import org.example.storageservice.application.repository.InventoryReservationRepository;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.application.repository.PartStockRepository;
import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.Color;
import org.example.storageservice.domain.car.enums.DrivetrainType;
import org.example.storageservice.domain.car.enums.FuelType;
import org.example.storageservice.domain.car.enums.GearBoxType;
import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.domain.reservation.enums.ReservationStatus;
import org.example.storageservice.infrastructure.inmemory.InMemoryAssemblyOrderRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryCarModelRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryCarRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryInventoryReservationRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryPartRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryPartStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class InventoryReservationServiceTest {

    private InventoryReservationService reservationService;
    private InventoryReservationRepository reservationRepository;
    private AssemblyOrderRepository assemblyOrderRepository;
    private CarModelService carModelService;
    private CarService carService;
    private PartService partService;
    private PartStockService partStockService;

    @BeforeEach
    void setUp() {
        assemblyOrderRepository = new InMemoryAssemblyOrderRepository();
        CarRepository carRepository = new InMemoryCarRepository();
        CarModelRepository carModelRepository = new InMemoryCarModelRepository();
        PartRepository partRepository = new InMemoryPartRepository();
        PartStockRepository partStockRepository = new InMemoryPartStockRepository();
        reservationRepository = new InMemoryInventoryReservationRepository();

        carModelService = new CarModelService(carModelRepository, partRepository);
        carService = new CarService(carRepository, carModelRepository);
        partService = new PartService(partRepository, carModelRepository);
        partStockService = new PartStockService(partStockRepository, partRepository);
        ConfiguratorService configuratorService = new ConfiguratorService(carModelRepository, partRepository);
        AssemblyOrderService assemblyOrderService = new AssemblyOrderService(
                assemblyOrderRepository, carRepository, carModelRepository, partRepository, partStockService);
        reservationService = new InventoryReservationService(
                reservationRepository, carRepository, assemblyOrderRepository, configuratorService, partStockService,
                assemblyOrderService, mock(InventoryReservationEventPublisher.class));
    }

    @Test
    void reserveStockCar_blocksSecondOrderAndCanBeConfirmed() {
        CarResponse car = carService.createCar(new SaveCarRequest(
                createBaseModel().id(), Color.BLACK, BigDecimal.valueOf(5000000)));
        UUID orderId = UUID.randomUUID();

        var reservation = reservationService.reserveStockCar(
                orderId, car.id(), future(), "trace-one");

        assertEquals(ReservationStatus.HELD, reservation.getStatus());
        assertThrows(DomainValidationException.class, () -> reservationService.reserveStockCar(
                UUID.randomUUID(), car.id(), future(), "trace-two"));

        var confirmed = reservationService.confirmReservation(orderId, reservation.getId(), "trace-one");
        assertEquals(ReservationStatus.CONFIRMED, confirmed.getStatus());
    }

    @Test
    void reserveConfiguration_holdsAndReleaseReturnsPartStock() {
        ConfiguredInventory inventory = createConfiguredInventory();
        UUID orderId = UUID.randomUUID();

        var reservation = reservationService.reserveConfiguration(
                orderId, inventory.model().id(), Map.of("WHEELS", inventory.part().id()), future(), "trace");
        assertEquals(1, partStockService.getPartStock(inventory.stockId()).reservedQuantity());

        var released = reservationService.releaseReservation(orderId, reservation.getId(), "cancelled", "trace");

        assertEquals(ReservationStatus.RELEASED, released.getStatus());
        assertEquals(0, partStockService.getPartStock(inventory.stockId()).reservedQuantity());
    }

    @Test
    void confirmCustomReservation_startsAssemblyAndAssemblyConsumesHeldParts() {
        ConfiguredInventory inventory = createConfiguredInventory();
        UUID orderId = UUID.randomUUID();
        var reservation = reservationService.reserveConfiguration(
                orderId, inventory.model().id(), Map.of("WHEELS", inventory.part().id()), future(), "trace");

        var confirmed = reservationService.confirmReservation(orderId, reservation.getId(), "trace");

        assertEquals(ReservationStatus.CONFIRMED, confirmed.getStatus());
        assertEquals(3, partStockService.getPartStock(inventory.stockId()).quantity());
        assertEquals(1, partStockService.getPartStock(inventory.stockId()).reservedQuantity());

        var assemblyOrder = assemblyOrderRepository.findAllBySourceOrderId(orderId).getFirst();
        reservationService.assemble(assemblyOrder.getId(), "trace");

        assertEquals(ReservationStatus.FULFILLED,
                reservationRepository.findById(reservation.getId()).orElseThrow().getStatus());
        assertEquals(2, partStockService.getPartStock(inventory.stockId()).quantity());
        assertEquals(0, partStockService.getPartStock(inventory.stockId()).reservedQuantity());
    }

    @Test
    void expireHeldConfiguration_releasesParts() {
        ConfiguredInventory inventory = createConfiguredInventory();
        var reservation = reservationService.reserveConfiguration(
                UUID.randomUUID(), inventory.model().id(), Map.of("WHEELS", inventory.part().id()), future(), "trace");
        reservation.setExpiresAt(Instant.now().minusSeconds(1));
        reservationRepository.save(reservation);

        reservationService.expireHeldReservations();

        assertEquals(ReservationStatus.EXPIRED, reservationRepository.findById(reservation.getId()).orElseThrow().getStatus());
        assertEquals(0, partStockService.getPartStock(inventory.stockId()).reservedQuantity());
    }

    private ConfiguredInventory createConfiguredInventory() {
        CarModelResponse model = createBaseModel();
        PartResponse part = partService.createPart(new SavePartRequest(
                "Sport wheels", PartType.WHEELS, BigDecimal.valueOf(120000), Set.of(model.id())));
        model = carModelService.updateCarModel(model.id(), new SaveCarModelRequest(
                "Configurable", Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE,
                300, 3.0, GearBoxType.AUTOMATIC, DrivetrainType.AWD,
                BigDecimal.valueOf(3000000), Map.of(PartType.WHEELS, part.id())));
        var stock = partStockService.createPartStock(new SavePartStockRequest(part.id(), 3, 0));
        return new ConfiguredInventory(model, part, stock.id());
    }

    private CarModelResponse createBaseModel() {
        return carModelService.createCarModel(new SaveCarModelRequest(
                "Configurable", Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE,
                300, 3.0, GearBoxType.AUTOMATIC, DrivetrainType.AWD,
                BigDecimal.valueOf(3000000), Map.of()));
    }

    private Instant future() {
        return Instant.now().plusSeconds(900);
    }

    private record ConfiguredInventory(CarModelResponse model, PartResponse part, UUID stockId) {
    }
}

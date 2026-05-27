package org.example.storageservice.application.service;

import org.example.storageservice.application.dto.request.SaveAssemblyOrderRequest;
import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.request.SaveCarRequest;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.request.SavePartStockRequest;
import org.example.storageservice.application.dto.response.AssemblyOrderResponse;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.repository.AssemblyOrderRepository;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.CarRepository;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.application.repository.PartStockRepository;
import org.example.storageservice.domain.assembly.enums.AssemblyOrderStatus;
import org.example.storageservice.domain.assembly.enums.SourceOrderType;
import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.Color;
import org.example.storageservice.domain.car.enums.DrivetrainType;
import org.example.storageservice.domain.car.enums.FuelType;
import org.example.storageservice.domain.car.enums.GearBoxType;
import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.infrastructure.inmemory.InMemoryAssemblyOrderRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryCarModelRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryCarRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryPartRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryPartStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssemblyOrderServiceTest {

    private AssemblyOrderService assemblyOrderService;
    private PartStockService partStockService;
    private CarModelService carModelService;
    private CarService carService;
    private PartService partService;

    @BeforeEach
    void setUp() {
        AssemblyOrderRepository assemblyOrderRepository = new InMemoryAssemblyOrderRepository();
        CarRepository carRepository = new InMemoryCarRepository();
        CarModelRepository carModelRepository = new InMemoryCarModelRepository();
        PartRepository partRepository = new InMemoryPartRepository();
        PartStockRepository partStockRepository = new InMemoryPartStockRepository();

        carModelService = new CarModelService(carModelRepository, partRepository);
        carService = new CarService(carRepository, carModelRepository);
        partService = new PartService(partRepository, carModelRepository);
        partStockService = new PartStockService(partStockRepository, partRepository);
        assemblyOrderService = new AssemblyOrderService(
                assemblyOrderRepository, carRepository, carModelRepository, partRepository, partStockService);
    }

    @Test
    void create_CommonOrderRequiresExistingCar() {
        CarResponse car = createCar(createModel().id());

        AssemblyOrderResponse order = assemblyOrderService.createAssemblyOrder(new SaveAssemblyOrderRequest(
                UUID.randomUUID(), SourceOrderType.COMMON, car.id(), null,
                Map.of(), UUID.randomUUID(), null, null));

        assertEquals(car.id(), order.carId());
        assertEquals(AssemblyOrderStatus.CREATED, order.status());
    }

    @Test
    void create_CustomOrderRequiresExistingModelAndParts() {
        CarModelResponse model = createModel();
        PartResponse part = createPart();

        AssemblyOrderResponse order = assemblyOrderService.createAssemblyOrder(new SaveAssemblyOrderRequest(
                UUID.randomUUID(), SourceOrderType.CUSTOM, null, model.id(),
                Map.of("WHEELS", part.id()), UUID.randomUUID(), null, null));

        assertEquals(model.id(), order.modelId());
        assertEquals(part.id(), order.requiredPartIds().get("WHEELS"));
    }

    @Test
    void create_CommonOrderWithoutCarRejected() {
        assertThrows(DomainValidationException.class,
                () -> assemblyOrderService.createAssemblyOrder(new SaveAssemblyOrderRequest(
                        UUID.randomUUID(), SourceOrderType.COMMON, null, null,
                        Map.of(), null, null, null)));
    }

    @Test
    void create_UnknownPartRejected() {
        CarModelResponse model = createModel();

        assertThrows(EntityNotFoundException.class,
                () -> assemblyOrderService.createAssemblyOrder(new SaveAssemblyOrderRequest(
                        UUID.randomUUID(), SourceOrderType.CUSTOM, null, model.id(),
                        Map.of("WHEELS", UUID.randomUUID()), null, null, null)));
    }

    @Test
    void assemble_ReservesRequiredPartsAndMarksAssembled() {
        CarModelResponse model = createModel();
        PartResponse part = createPart();
        var stock = partStockService.createPartStock(new SavePartStockRequest(part.id(), 3, 0));
        AssemblyOrderResponse order = assemblyOrderService.createAssemblyOrder(new SaveAssemblyOrderRequest(
                UUID.randomUUID(), SourceOrderType.CUSTOM, null, model.id(),
                Map.of("WHEELS", part.id()), UUID.randomUUID(), null, null));

        AssemblyOrderResponse assembled = assemblyOrderService.assemble(order.id());

        assertEquals(AssemblyOrderStatus.ASSEMBLED, assembled.status());
        assertEquals(1, partStockService.getPartStock(stock.id()).reservedQuantity());
    }

    @Test
    void fail_MarksAssemblyOrderAsFail() {
        CarResponse car = createCar(createModel().id());
        AssemblyOrderResponse order = assemblyOrderService.createAssemblyOrder(new SaveAssemblyOrderRequest(
                UUID.randomUUID(), SourceOrderType.COMMON, car.id(), null,
                Map.of(), UUID.randomUUID(), null, null));

        AssemblyOrderResponse failed = assemblyOrderService.failToAssemble(order.id(), "Not enough stock");

        assertEquals(AssemblyOrderStatus.FAIL, failed.status());
        assertEquals("Not enough stock", failed.failureReason());
    }

    private CarModelResponse createModel() {
        return carModelService.createCarModel(new SaveCarModelRequest(
                "M5", Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE,
                500, 4.4, GearBoxType.AUTOMATIC, DrivetrainType.AWD,
                BigDecimal.valueOf(5000000), Map.of()));
    }

    private CarResponse createCar(UUID modelId) {
        return carService.createCar(new SaveCarRequest(modelId, Color.BLACK, BigDecimal.valueOf(5500000)));
    }

    private PartResponse createPart() {
        return partService.createPart(new SavePartRequest(
                "Wheels", PartType.WHEELS, BigDecimal.valueOf(120000), Set.of()));
    }
}

package org.example.storageservice.application.service;

import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.request.SaveCarRequest;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.CarRepository;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.Color;
import org.example.storageservice.domain.car.enums.DrivetrainType;
import org.example.storageservice.domain.car.enums.FuelType;
import org.example.storageservice.domain.car.enums.GearBoxType;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.infrastructure.inmemory.InMemoryCarModelRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryCarRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryPartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CarModelServiceTest {

    private CarModelService carModelService;
    private CarService carService;
    private PartService partService;

    @BeforeEach
    void setUp() {
        CarRepository carRepository = new InMemoryCarRepository();
        CarModelRepository carModelRepository = new InMemoryCarModelRepository();
        PartRepository partRepository = new InMemoryPartRepository();

        carModelService = new CarModelService(carModelRepository, partRepository);
        carService = new CarService(carRepository, carModelRepository);
        partService = new PartService(partRepository, carModelRepository);
    }

    private SaveCarModelRequest modelRequest(String name, Map<PartType, UUID> parts) {
        return new SaveCarModelRequest(name, Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE,
                184, 2.0, GearBoxType.AUTOMATIC, DrivetrainType.RWD,
                BigDecimal.valueOf(3000000), parts);
    }

    @Test
    void create_PersistModel() {
        SaveCarModelRequest req = modelRequest("320i", Map.of());
        CarModelResponse res = carModelService.createCarModel(req);

        assertNotNull(res.id());
        assertEquals(req.modelName(), res.modelName());
        assertEquals(req.brand(), res.brand());
        assertEquals(req.basePrice(), res.basePrice());
    }

    @Test
    void create_withNonExistentPart() {
        assertThrows(EntityNotFoundException.class,
                () -> carModelService.createCarModel(
                        modelRequest("320i", Map.of(PartType.WHEELS, UUID.randomUUID()))));
    }

    @Test
    void create_withExistingPart() {
        UUID partId = partService.createPart(
                new SavePartRequest("17'' Std", PartType.WHEELS, BigDecimal.ZERO, Set.of())).id();

        CarModelResponse res = carModelService.createCarModel(
                modelRequest("320i", Map.of(PartType.WHEELS, partId)));

        assertEquals(partId, res.basePartIds().get(PartType.WHEELS));
    }

    @Test
    void get_ReturnModel() {
        CarModelResponse created = carModelService.createCarModel(modelRequest("320i", Map.of()));

        assertEquals(created.id(), carModelService.getCarModel(created.id()).id());
    }

    @Test
    void get_notFound() {
        assertThrows(EntityNotFoundException.class, () -> carModelService.getCarModel(UUID.randomUUID()));
    }

    @Test
    void list_ReturnAllModels() {
        carModelService.createCarModel(modelRequest("320i", Map.of()));
        carModelService.createCarModel(modelRequest("330i", Map.of()));

        assertEquals(2, carModelService.listCarModels().size());
    }

    @Test
    void update_ApplyChanges() {
        CarModelResponse created = carModelService.createCarModel(modelRequest("320i", Map.of()));
        SaveCarModelRequest updateReq = modelRequest("320i xDrive", Map.of());

        CarModelResponse updated = carModelService.updateCarModel(created.id(), updateReq);

        assertEquals(created.id(), updated.id());
        assertEquals(updateReq.modelName(), updated.modelName());
    }

    @Test
    void update_notFound() {
        assertThrows(EntityNotFoundException.class,
                () -> carModelService.updateCarModel(UUID.randomUUID(), modelRequest("X", Map.of())));
    }

    @Test
    void delete_RemoveModel() {
        CarModelResponse model = carModelService.createCarModel(modelRequest("320i", Map.of()));
        carModelService.deleteCarModel(model.id());

        assertThrows(EntityNotFoundException.class, () -> carModelService.getCarModel(model.id()));
    }

    @Test
    void delete_notFound() {
        assertThrows(EntityNotFoundException.class,
                () -> carModelService.deleteCarModel(UUID.randomUUID()));
    }

    @Test
    void car_referencesModelById() {
        SaveCarModelRequest modelReq = modelRequest("320i", Map.of());
        CarModelResponse model = carModelService.createCarModel(modelReq);
        var car = carService.createCar(new SaveCarRequest(model.id(), Color.BLACK, BigDecimal.valueOf(3200000)));

        assertEquals(model.id(), car.modelId());
        // The base price of the model is independent of the price of the car in stock.
        assertEquals(modelReq.basePrice(), model.basePrice());
        assertEquals(0, BigDecimal.valueOf(3200000).compareTo(car.price()));
    }

    @Test
    void car_deletedModel() {
        CarModelResponse model = carModelService.createCarModel(modelRequest("320i", Map.of()));
        carModelService.deleteCarModel(model.id());

        assertThrows(EntityNotFoundException.class,
                () -> carService.createCar(new SaveCarRequest(model.id(), Color.BLACK, BigDecimal.valueOf(3000000))));
    }
}

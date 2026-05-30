package org.example.storageservice.application.service;

import org.example.storageservice.application.dto.request.CarFilterRequest;
import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.request.SaveCarRequest;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.CarResponse;
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
import org.example.storageservice.infrastructure.inmemory.InMemoryCarModelRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryCarRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryPartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CatalogServiceTest {

    private CatalogService catalogService;
    private CarService carService;
    private CarModelService carModelService;

    private CarResponse car320Black, carX5Blue, carMustangRed;
    private CarModelResponse model320i;

    @BeforeEach
    void setUp() {
        CarRepository carRepository = new InMemoryCarRepository();
        CarModelRepository carModelRepository = new InMemoryCarModelRepository();
        PartRepository partRepository = new InMemoryPartRepository();
        carModelService = new CarModelService(carModelRepository, partRepository);
        carService = new CarService(carRepository, carModelRepository);
        catalogService = new CatalogService(carRepository, carModelRepository, partRepository);

        model320i = createModel("320i", Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE, 184, 2.0,
                GearBoxType.AUTOMATIC, DrivetrainType.RWD, 3000000);

        CarModelResponse model330i = createModel("330i", Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE, 258,
                2.0, GearBoxType.AUTOMATIC, DrivetrainType.RWD, 4000000);

        CarModelResponse modelX5 = createModel("X5", Brand.BMW, BodyType.SUV, FuelType.DIESEL, 340,
                3.0, GearBoxType.AUTOMATIC, DrivetrainType.AWD, 6000000);

        CarModelResponse modelMustang = createModel("Mustang", Brand.FORD, BodyType.COUPE, FuelType.GASOLINE,
                450, 5.0, GearBoxType.MANUAL,    DrivetrainType.RWD, 5500000);

        car320Black = createCar(model320i.id(), Color.BLACK, 3200000);
        createCar(model320i.id(), Color.WHITE, 3500000);
        createCar(model330i.id(), Color.RED, 4500000);
        carX5Blue = createCar(modelX5.id(), Color.BLUE, 6800000);
        carMustangRed = createCar(modelMustang.id(), Color.RED, 5900000);
    }

    private CarModelResponse createModel(String name, Brand brand, BodyType body, FuelType fuel,
                                          int power, double volume, GearBoxType gear,
                                          DrivetrainType drive, long basePrice) {
        return carModelService.createCarModel(new SaveCarModelRequest(
                name, brand, body, fuel, power, volume, gear, drive,
                BigDecimal.valueOf(basePrice), Map.of()));
    }

    private CarResponse createCar(UUID modelId, Color color, long price) {
        return carService.createCar(new SaveCarRequest(modelId, color, BigDecimal.valueOf(price)));
    }

    private CarFilterRequest emptyFilter() {
        return new CarFilterRequest(null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null);
    }

    @Test
    void getCar_ReturnCar() {
        CarResponse found = catalogService.getCar(car320Black.id());

        assertEquals(car320Black.id(), found.id());
        assertEquals(model320i.id(), found.modelId());
    }

    @Test
    void getCar_notFound() {
        assertThrows(EntityNotFoundException.class, () -> catalogService.getCar(UUID.randomUUID()));
    }

    @Test
    void list_FilterReturnsAll() {
        assertEquals(5, catalogService.listCars(emptyFilter()).size());
    }

    @Test
    void filter_minPrice() {
        // >= 5000000 -> X5, Mustang
        assertEquals(2, catalogService.listCars(
                new CarFilterRequest(5000000.0, null, null, null, null,
                        null, null, null, null, null,
                        null, null, null, null)).size());
    }

    @Test
    void filter_maxPrice() {
        // <= 3500000 -> 320Black, 320White
        assertEquals(2, catalogService.listCars(
                new CarFilterRequest(null, 3500000.0, null, null, null,
                        null, null, null, null, null,
                        null, null, null, null)).size());
    }

    @Test
    void filter_brand() {
        // BMW -> 4 cars
        assertEquals(4, catalogService.listCars(
                new CarFilterRequest(null, null, Brand.BMW, null, null,
                        null, null, null, null, null,
                        null, null, null, null)).size());
    }

    @Test
    void filter_bodyType() {
        // SUV -> carX5Blue
        List<CarResponse> result = catalogService.listCars(
                new CarFilterRequest(null, null, null, null, BodyType.SUV,
                        null, null, null, null, null,
                        null, null, null, null));

        assertEquals(1, result.size());
        assertEquals(carX5Blue.id(), result.getFirst().id());
    }

    @Test
    void filter_fuelType() {
        // DIESEL -> carX5Blue
        List<CarResponse> result = catalogService.listCars(
                new CarFilterRequest(null, null, null, null, null,
                        FuelType.DIESEL, null, null, null,
                        null, null, null, null, null));

        assertEquals(1, result.size());
        assertEquals(carX5Blue.id(), result.getFirst().id());
    }

    @Test
    void filter_minEnginePower() {
        // >= 340hp -> X5, Mustang
        assertEquals(2, catalogService.listCars(
                new CarFilterRequest(null, null, null, null, null,
                        null, 340, null, null, null,
                        null, null, null, null)).size());
    }

    @Test
    void filter_maxEnginePower() {
        // <= 200hp -> 320i x2
        assertEquals(2, catalogService.listCars(
                new CarFilterRequest(null, null, null, null, null,
                        null, null, 200, null, null,
                        null, null, null, null)).size());
    }

    @Test
    void filter_minEngineVolume() {
        // >= 3.0L -> X5, Mustang
        assertEquals(2, catalogService.listCars(
                new CarFilterRequest(null, null, null, null, null,
                        null, null, null, 3.0, null,
                        null, null, null, null)).size());
    }

    @Test
    void filter_maxEngineVolume() {
        // <= 2.0L -> 320i x2, 330i x1
        assertEquals(3, catalogService.listCars(
                new CarFilterRequest(null, null, null, null, null,
                        null, null, null, null, 2.0,
                        null, null, null, null)).size());
    }

    @Test
    void filter_gearBox() {
        // MANUAL -> Mustang
        List<CarResponse> result = catalogService.listCars(
                new CarFilterRequest(null, null, null, null, null,
                        null, null, null, null, null,
                        GearBoxType.MANUAL, null, null, null));

        assertEquals(1, result.size());
        assertEquals(carMustangRed.id(), result.getFirst().id());
    }

    @Test
    void filter_drivetrain() {
        // AWD -> carX5Blue
        List<CarResponse> result = catalogService.listCars(
                new CarFilterRequest(null, null, null, null, null,
                        null, null, null, null, null,
                        null, DrivetrainType.AWD, null, null));

        assertEquals(1, result.size());
        assertEquals(carX5Blue.id(), result.getFirst().id());
    }

    @Test
    void filter_color() {
        // RED -> car330Red + carMustangRed
        List<CarResponse> result = catalogService.listCars(
                new CarFilterRequest(null, null, null, null, null,
                        null, null, null, null, null,
                        null, null, Color.RED, null));

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.color() == Color.RED));
    }
}

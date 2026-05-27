package org.example.storageservice.application.service;

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
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CarServiceTest {

    private CarService carService;
    private CarModelService carModelService;

    @BeforeEach
    void setUp() {
        CarRepository carRepository = new InMemoryCarRepository();
        CarModelRepository carModelRepository = new InMemoryCarModelRepository();
        PartRepository partRepository = new InMemoryPartRepository();
        carService = new CarService(carRepository, carModelRepository);
        carModelService = new CarModelService(carModelRepository, partRepository);
    }

    private CarModelResponse anyModel() {
        return carModelService.createCarModel(new SaveCarModelRequest(
                "320i", Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE,
                184, 2.0, GearBoxType.AUTOMATIC, DrivetrainType.RWD,
                BigDecimal.valueOf(3000000), Map.of()));
    }

    @Test
    void create_PersistCar() {
        CarModelResponse model = anyModel();
        SaveCarRequest req = new SaveCarRequest(model.id(), Color.BLACK, BigDecimal.valueOf(3200000));
        CarResponse car = carService.createCar(req);

        assertNotNull(car.id());
        assertEquals(req.modelId(), car.modelId());
        assertEquals(req.color(), car.color());
    }

    @Test
    void create_unknownModel() {
        assertThrows(EntityNotFoundException.class,
                () -> carService.createCar(new SaveCarRequest(UUID.randomUUID(), Color.BLACK,
                        BigDecimal.valueOf(1000000))));
    }

    @Test
    void get_ReturnCar() {
        CarModelResponse model = anyModel();
        CarResponse created = carService.createCar(new SaveCarRequest(model.id(), Color.RED,
                BigDecimal.valueOf(3000000)));

        assertEquals(created.id(), carService.getCar(created.id()).id());
    }

    @Test
    void get_notFound() {
        assertThrows(EntityNotFoundException.class, () -> carService.getCar(UUID.randomUUID()));
    }

    @Test
    void list_ReturnAllCars() {
        CarModelResponse model = anyModel();
        carService.createCar(new SaveCarRequest(model.id(), Color.BLACK, BigDecimal.valueOf(3000000)));
        carService.createCar(new SaveCarRequest(model.id(), Color.WHITE, BigDecimal.valueOf(3200000)));

        assertEquals(2, carService.listCars().size());
    }

    @Test
    void update_ApplyChanges() {
        CarModelResponse model = anyModel();
        CarResponse car = carService.createCar(new SaveCarRequest(model.id(), Color.BLACK,
                BigDecimal.valueOf(3000000)));
        SaveCarRequest updateReq = new SaveCarRequest(model.id(), Color.RED, BigDecimal.valueOf(3500000));
        CarResponse updated = carService.updateCar(car.id(), updateReq);

        assertEquals(car.id(), updated.id());
        assertEquals(updateReq.color(), updated.color());
        assertEquals(0, updateReq.price().compareTo(updated.price()));
    }

    @Test
    void update_notFound() {
        assertThrows(EntityNotFoundException.class,
                () -> carService.updateCar(UUID.randomUUID(),
                        new SaveCarRequest(anyModel().id(), Color.RED, BigDecimal.valueOf(1000000))));
    }

    @Test
    void delete_RemoveCar() {
        CarModelResponse model = anyModel();
        CarResponse car =
                carService.createCar(new SaveCarRequest(model.id(), Color.BLACK, BigDecimal.valueOf(3000000)));
        carService.deleteCar(car.id());

        assertThrows(EntityNotFoundException.class, () -> carService.getCar(car.id()));
    }

    @Test
    void delete_notFound() {
        assertThrows(EntityNotFoundException.class, () -> carService.deleteCar(UUID.randomUUID()));
    }
}

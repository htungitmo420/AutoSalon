package org.example.storageservice.application.service;

import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.DrivetrainType;
import org.example.storageservice.domain.car.enums.FuelType;
import org.example.storageservice.domain.car.enums.GearBoxType;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.infrastructure.inmemory.InMemoryCarModelRepository;
import org.example.storageservice.infrastructure.inmemory.InMemoryPartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PartServiceTest {

    private PartService partService;
    private CarModelService carModelService;

    @BeforeEach
    void setUp() {
        CarModelRepository carModelRepository = new InMemoryCarModelRepository();
        PartRepository partRepository = new InMemoryPartRepository();
        partService = new PartService(partRepository, carModelRepository);
        carModelService = new CarModelService(carModelRepository, partRepository);
    }

    private CarModelResponse anyModel() {
        return carModelService.createCarModel(new SaveCarModelRequest(
                "320i", Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE,
                184, 2.0, GearBoxType.AUTOMATIC, DrivetrainType.RWD,
                BigDecimal.valueOf(3000000), Map.of()));
    }

    @Test
    void create_PersistPart() {
        SavePartRequest req = new SavePartRequest("17'' Standard", PartType.WHEELS, BigDecimal.ZERO, Set.of());
        PartResponse res = partService.createPart(req);

        assertNotNull(res.id());
        assertEquals(req.name(), res.name());
        assertEquals(req.type(), res.type());
    }

    @Test
    void create_withCompatibleModel() {
        CarModelResponse model = anyModel();
        PartResponse res = partService.createPart(new SavePartRequest("19'' M-Sport", PartType.WHEELS,
                BigDecimal.valueOf(95000), Set.of(model.id())));

        assertTrue(res.compatibleModelIds().contains(model.id()));
    }

    @Test
    void create_unknownCompatibleModel() {
        assertThrows(EntityNotFoundException.class,
                () -> partService.createPart(
                        new SavePartRequest("X", PartType.WHEELS, BigDecimal.ZERO, Set.of(UUID.randomUUID()))));
    }

    @Test
    void get_ReturnPart() {
        PartResponse created = partService.createPart(
                new SavePartRequest("Steering", PartType.STEERING_WHEEL, BigDecimal.valueOf(25000), Set.of()));
        assertEquals(created.id(), partService.getPart(created.id()).id());
    }

    @Test
    void get_notFound() {
        assertThrows(EntityNotFoundException.class, () -> partService.getPart(UUID.randomUUID()));
    }

    @Test
    void list_ReturnAllParts() {
        partService.createPart(new SavePartRequest("A", PartType.WHEELS, BigDecimal.ZERO, Set.of()));
        partService.createPart(new SavePartRequest("B", PartType.INTERIOR, BigDecimal.valueOf(100000), Set.of()));

        assertEquals(2, partService.listParts().size());
    }

    @Test
    void update_ApplyChanges() {
        PartResponse created = partService.createPart(
                new SavePartRequest("Old", PartType.WHEELS, BigDecimal.ZERO, Set.of()));

        CarModelResponse model = anyModel();

        SavePartRequest updateReq = new SavePartRequest("New", PartType.WHEELS,
                BigDecimal.valueOf(50000), Set.of(model.id()));

        PartResponse updated = partService.updatePart(created.id(), updateReq);

        assertEquals(created.id(), updated.id());
        assertEquals(updateReq.name(), updated.name());
        assertEquals(updateReq.surcharge(), updated.surcharge());
    }

    @Test
    void update_notFound() {
        assertThrows(EntityNotFoundException.class,
                () -> partService.updatePart(UUID.randomUUID(),
                        new SavePartRequest("X", PartType.WHEELS, BigDecimal.ZERO, Set.of())));
    }

    @Test
    void delete_RemovePart() {
        PartResponse created = partService.createPart(
                new SavePartRequest("Del", PartType.WHEELS, BigDecimal.ZERO, Set.of()));
        partService.deletePart(created.id());
        assertThrows(EntityNotFoundException.class, () -> partService.getPart(created.id()));
    }

    @Test
    void delete_notFound() {
        assertThrows(EntityNotFoundException.class, () -> partService.deletePart(UUID.randomUUID()));
    }
}

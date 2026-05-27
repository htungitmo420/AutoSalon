package org.example.storageservice.application.service;

import org.example.storageservice.application.dto.request.CarConfigurationRequest;
import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.response.CarConfigurationResponse;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.DrivetrainType;
import org.example.storageservice.domain.car.enums.FuelType;
import org.example.storageservice.domain.car.enums.GearBoxType;
import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.example.storageservice.domain.exceptions.IncompatibleComponentException;
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

class ConfiguratorServiceTest {

    private CarModelService carModelService;
    private PartService partService;
    private ConfiguratorService configuratorService;

    private CarModelResponse model320i;

    // upgrades
    private PartResponse wheels19MSport; // +95000
    private PartResponse steering6MT; // -30000
    private PartResponse steeringMSport; // +25000
    private PartResponse interiorDakota; // +110000
    private PartResponse interiorPerf; // not compatible with 320i

    @BeforeEach
    void setUp() {
        CarModelRepository carModelRepository = new InMemoryCarModelRepository();
        PartRepository partRepository = new InMemoryPartRepository();
        carModelService = new CarModelService(carModelRepository, partRepository);
        partService = new PartService(partRepository, carModelRepository);
        configuratorService = new ConfiguratorService(carModelRepository, partRepository);

        PartResponse wheels17Std = create("17'' Standard", PartType.WHEELS, 0, Set.of());
        PartResponse transmission8AT = create("Automatic 8AT", PartType.TRANSMISSION, 0, Set.of());
        PartResponse steeringSdt = create("Sport Leather Standard", PartType.STEERING_WHEEL,0, Set.of());
        PartResponse interiorGraphite = create("Fabric Graphite", PartType.INTERIOR, 0, Set.of());

        steeringMSport = create("M-Sport с подогревом", PartType.STEERING_WHEEL, 25000, Set.of());
        wheels19MSport = create("19'' M-Sport", PartType.WHEELS, 95000, Set.of());
        steering6MT = create("Manual 6MT", PartType.TRANSMISSION, -30000, Set.of());
        interiorDakota = create("Leather Dakota", PartType.INTERIOR, 110000, Set.of());
        interiorPerf = create("Sport Performance", PartType.INTERIOR, 160000, Set.of());

        model320i = createBmw320i(
                Map.of(PartType.WHEELS, wheels17Std.id(),
                       PartType.TRANSMISSION, transmission8AT.id(),
                       PartType.STEERING_WHEEL, steeringSdt.id(),
                       PartType.INTERIOR, interiorGraphite.id()));

        // Mark parts compatible with 320i
        UUID mid = model320i.id();
        updateCompat(wheels17Std, mid);
        updateCompat(wheels19MSport, mid);
        updateCompat(steering6MT, mid);
        updateCompat(transmission8AT, mid);
        updateCompat(steeringSdt, mid);
        updateCompat(steeringMSport, mid);
        updateCompat(interiorGraphite, mid);
        updateCompat(interiorDakota, mid);
    }

    private CarModelResponse createBmw320i(Map<PartType, UUID> parts) {
        return carModelService.createCarModel(new SaveCarModelRequest(
                "BMW 320i", Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE,
                184, 2.0, GearBoxType.AUTOMATIC, DrivetrainType.RWD,
                BigDecimal.valueOf(3000000), parts));
    }

    private PartResponse create(String name, PartType type, long surcharge, Set<UUID> models) {
        return partService.createPart(new SavePartRequest(name, type, BigDecimal.valueOf(surcharge), models));
    }

    private void updateCompat(PartResponse p, UUID modelId) {
        partService.updatePart(p.id(),
                new SavePartRequest(p.name(), p.type(), p.surcharge(), Set.of(modelId)));
    }

    @Test
    void baseConfig_ReturnBasePrice() {
        CarConfigurationResponse res = configuratorService.buildConfiguration(
                model320i.id(), new CarConfigurationRequest(Map.of()));

        assertEquals(0, BigDecimal.valueOf(3000000).compareTo(res.totalPrice()));
        assertEquals(4, res.selectedParts().size());
    }

    @Test
    void upgrade_AddSurcharge() {
        // 19'' M-Sport(+95k) + M-Sport steering(+25k) + Dakota(+110k) = +230k
        CarConfigurationResponse res = configuratorService.buildConfiguration(
                model320i.id(),
                new CarConfigurationRequest(Map.of(
                        PartType.WHEELS, wheels19MSport.id(),
                        PartType.STEERING_WHEEL, steeringMSport.id(),
                        PartType.INTERIOR, interiorDakota.id())));

        assertEquals(0, BigDecimal.valueOf(3000000 + 230000).compareTo(res.totalPrice()));
    }

    @Test
    void downgrade_DeductSurcharge() {
        CarConfigurationResponse res = configuratorService.buildConfiguration(
                model320i.id(),
                new CarConfigurationRequest(Map.of(PartType.TRANSMISSION, steering6MT.id())));

        assertEquals(0, BigDecimal.valueOf(3000000 - 30000).compareTo(res.totalPrice()));
    }

    @Test
    void incompatiblePart() {
        assertThrows(IncompatibleComponentException.class, () ->
                configuratorService.buildConfiguration(model320i.id(),
                        new CarConfigurationRequest(Map.of(PartType.INTERIOR, interiorPerf.id()))));
    }

    @Test
    void unknownSlot() {
        assertThrows(DomainValidationException.class, () ->
                configuratorService.buildConfiguration(model320i.id(),
                        new CarConfigurationRequest(Map.of(PartType.OTHER, wheels19MSport.id()))));
    }

    @Test
    void modelNotFound() {
        assertThrows(EntityNotFoundException.class, () ->
                configuratorService.buildConfiguration(UUID.randomUUID(), new CarConfigurationRequest(Map.of())));
    }

    @Test
    void partNotFound() {
        assertThrows(EntityNotFoundException.class, () ->
                configuratorService.buildConfiguration(model320i.id(),
                        new CarConfigurationRequest(Map.of(PartType.WHEELS, UUID.randomUUID()))));
    }
}

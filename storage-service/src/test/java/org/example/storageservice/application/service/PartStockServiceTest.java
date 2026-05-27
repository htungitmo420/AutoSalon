package org.example.storageservice.application.service;

import org.example.storageservice.application.dto.request.SavePartStockRequest;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.dto.response.PartStockResponse;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.application.repository.PartStockRepository;
import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.infrastructure.inmemory.InMemoryCarModelRepository;
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

class PartStockServiceTest {

    private PartService partService;
    private PartStockService partStockService;

    @BeforeEach
    void setUp() {
        CarModelRepository carModelRepository = new InMemoryCarModelRepository();
        PartRepository partRepository = new InMemoryPartRepository();
        PartStockRepository partStockRepository = new InMemoryPartStockRepository();
        partService = new PartService(partRepository, carModelRepository);
        partStockService = new PartStockService(partStockRepository, partRepository);
    }

    @Test
    void create_PersistsStockForPart() {
        PartResponse part = createPart();

        PartStockResponse stock = partStockService.createPartStock(
                new SavePartStockRequest(part.id(), 10, 2));

        assertEquals(part.id(), stock.partId());
        assertEquals(8, stock.availableQuantity());
    }

    @Test
    void create_DuplicatePartStockRejected() {
        PartResponse part = createPart();
        partStockService.createPartStock(new SavePartStockRequest(part.id(), 10, 0));

        assertThrows(DomainValidationException.class,
                () -> partStockService.createPartStock(new SavePartStockRequest(part.id(), 5, 0)));
    }

    @Test
    void create_ReservedCannotExceedQuantity() {
        PartResponse part = createPart();

        assertThrows(DomainValidationException.class,
                () -> partStockService.createPartStock(new SavePartStockRequest(part.id(), 1, 2)));
    }

    @Test
    void create_UnknownPart() {
        assertThrows(EntityNotFoundException.class,
                () -> partStockService.createPartStock(new SavePartStockRequest(UUID.randomUUID(), 1, 0)));
    }

    @Test
    void reserveParts_IncreasesReservedQuantity() {
        PartResponse part = createPart();
        PartStockResponse stock = partStockService.createPartStock(new SavePartStockRequest(part.id(), 4, 1));

        partStockService.reserveParts(Map.of("WHEELS", part.id()));

        PartStockResponse updated = partStockService.getPartStock(stock.id());
        assertEquals(2, updated.reservedQuantity());
        assertEquals(2, updated.availableQuantity());
    }

    @Test
    void reserveParts_NotEnoughStock() {
        PartResponse part = createPart();
        partStockService.createPartStock(new SavePartStockRequest(part.id(), 1, 1));

        assertThrows(DomainValidationException.class,
                () -> partStockService.reserveParts(Map.of("WHEELS", part.id())));
    }

    private PartResponse createPart() {
        return partService.createPart(new SavePartRequest(
                "Stocked part", PartType.WHEELS, BigDecimal.ZERO, Set.of()));
    }
}

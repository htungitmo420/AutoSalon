package org.example.storageservice.infrastructure.inmemory;

import org.example.storageservice.domain.car.enums.Color;
import org.example.storageservice.domain.car.model.Car;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCarRepositoryTest {

    private Car newCar() {
        return Car.builder()
                .modelId(UUID.randomUUID())
                .color(Color.BLACK)
                .price(BigDecimal.valueOf(3000000))
                .build();
    }

    @Test
    void save_assignsIdOnFirstSave() {
        var repo = new InMemoryCarRepository();
        Car car = repo.save(newCar());

        assertNotNull(car.getId());
    }

    @Test
    void save_nullThrowsIllegalArgument() {
        var repo = new InMemoryCarRepository();

        assertThrows(IllegalArgumentException.class, () -> repo.save(null));
    }

    @Test
    void findById_returnsSavedCar() {
        var repo = new InMemoryCarRepository();
        Car car = repo.save(newCar());

        assertEquals(Color.BLACK, repo.findById(car.getId()).orElseThrow().getColor());
    }

    @Test
    void save_updateExisting_noDuplicate() {
        var repo = new InMemoryCarRepository();
        Car car = repo.save(newCar());
        car.setColor(Color.RED);
        repo.save(car);

        assertEquals(Color.RED, repo.findById(car.getId()).orElseThrow().getColor());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void deleteById_removesExistingCar() {
        var repo = new InMemoryCarRepository();
        Car car = repo.save(newCar());

        assertTrue(repo.deleteById(car.getId()));
        assertTrue(repo.findById(car.getId()).isEmpty());
    }

    @Test
    void deleteById_returnsFalseForMissingId() {
        var repo = new InMemoryCarRepository();

        assertFalse(repo.deleteById(UUID.randomUUID()));
    }
}
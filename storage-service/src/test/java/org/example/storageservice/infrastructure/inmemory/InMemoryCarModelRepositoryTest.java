package org.example.storageservice.infrastructure.inmemory;

import org.example.storageservice.domain.car.model.CarModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCarModelRepositoryTest {

    private CarModel newModel() {
        return CarModel.builder()
                .modelName("320i")
                .basePrice(BigDecimal.valueOf(3000000))
                .basePartIds(Map.of())
                .build();
    }

    @Test
    void save_assignsIdOnFirstSave() {
        var repo = new InMemoryCarModelRepository();
        CarModel model = repo.save(newModel());

        assertNotNull(model.getId());
    }

    @Test
    void save_nullThrowsIllegalArgument() {
        var repo = new InMemoryCarModelRepository();

        assertThrows(IllegalArgumentException.class, () -> repo.save(null));
    }

    @Test
    void findById_returnsSavedModel() {
        var repo = new InMemoryCarModelRepository();
        CarModel model = repo.save(newModel());

        assertEquals("320i", repo.findById(model.getId()).orElseThrow().getModelName());
    }

    @Test
    void save_updateExisting_noDuplicate() {
        var repo = new InMemoryCarModelRepository();
        CarModel model = repo.save(newModel());
        model.setModelName("320i xDrive");
        repo.save(model);

        assertEquals("320i xDrive", repo.findById(model.getId()).orElseThrow().getModelName());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void deleteById_removesExistingModel() {
        var repo = new InMemoryCarModelRepository();
        CarModel model = repo.save(newModel());

        assertTrue(repo.deleteById(model.getId()));
        assertTrue(repo.findById(model.getId()).isEmpty());
    }

    @Test
    void deleteById_returnsFalseForMissingId() {
        var repo = new InMemoryCarModelRepository();

        assertFalse(repo.deleteById(UUID.randomUUID()));
    }
}
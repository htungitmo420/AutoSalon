package org.example.storageservice.infrastructure.inmemory;

import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.domain.part.models.Part;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryPartRepositoryTest {

    private Part newPart(PartType type, String name) {
        return Part.builder()
                .type(type).name(name)
                .surcharge(BigDecimal.ZERO)
                .compatibleModelIds(Set.of()).build();
    }

    @Test
    void save_assignsIdOnFirstSave() {
        var repo = new InMemoryPartRepository();
        Part part = repo.save(newPart(PartType.WHEELS, "W1"));

        assertNotNull(part.getId());
    }

    @Test
    void save_nullThrowsIllegalArgument() {
        var repo = new InMemoryPartRepository();
        assertThrows(IllegalArgumentException.class, () -> repo.save(null));
    }

    @Test
    void save_updateExisting_noDuplicate() {
        var repo = new InMemoryPartRepository();
        Part part = repo.save(newPart(PartType.WHEELS, "W1"));
        part.setName("W1-updated");
        repo.save(part);

        assertEquals(1, repo.findByType(PartType.WHEELS).size());
    }

    @Test
    void findByType_returnsMatchingParts() {
        var repo = new InMemoryPartRepository();
        repo.save(newPart(PartType.WHEELS, "W1"));
        repo.save(newPart(PartType.WHEELS, "W2"));
        repo.save(newPart(PartType.INTERIOR, "I1"));

        assertEquals(2, repo.findByType(PartType.WHEELS).size());
        assertEquals(1, repo.findByType(PartType.INTERIOR).size());
    }

    @Test
    void findByType_returnsEmptyForNull() {
        var repo = new InMemoryPartRepository();
        assertTrue(repo.findByType(null).isEmpty());
    }

    @Test
    void deleteById_removesExistingPart() {
        var repo = new InMemoryPartRepository();
        Part part = repo.save(newPart(PartType.WHEELS, "W1"));
        repo.save(newPart(PartType.WHEELS, "W2"));

        assertTrue(repo.deleteById(part.getId()));
        assertEquals(1, repo.findByType(PartType.WHEELS).size());
    }

    @Test
    void deleteById_returnsFalseForMissingId() {
        var repo = new InMemoryPartRepository();
        assertFalse(repo.deleteById(UUID.randomUUID()));
    }
}
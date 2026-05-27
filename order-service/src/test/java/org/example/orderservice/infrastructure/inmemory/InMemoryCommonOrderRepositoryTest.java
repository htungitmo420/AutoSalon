package org.example.orderservice.infrastructure.inmemory;

import org.example.orderservice.domain.order.enums.CommonOrderStatus;
import org.example.orderservice.domain.order.model.CommonCarOrder;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCommonOrderRepositoryTest {

    private CommonCarOrder newOrder() {
        return CommonCarOrder.builder()
                .carId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .status(CommonOrderStatus.CREATED)
                .build();
    }

    @Test
    void save_assignsIdOnFirstSave() {
        var repo = new InMemoryCommonOrderRepository();
        CommonCarOrder order = repo.save(newOrder());
        assertNotNull(order.getId());
    }

    @Test
    void save_nullThrowsIllegalArgument() {
        var repo = new InMemoryCommonOrderRepository();
        assertThrows(IllegalArgumentException.class, () -> repo.save(null));
    }

    @Test
    void save_updateExisting_noDuplicate() {
        var repo = new InMemoryCommonOrderRepository();
        CommonCarOrder order = repo.save(newOrder());
        order.setStatus(CommonOrderStatus.APPROVED_BY_MANAGER);
        repo.save(order);

        assertEquals(CommonOrderStatus.APPROVED_BY_MANAGER,
                repo.findById(order.getId()).orElseThrow().getStatus());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void deleteById_removesExistingOrder() {
        var repo = new InMemoryCommonOrderRepository();
        CommonCarOrder order = repo.save(newOrder());

        assertTrue(repo.deleteById(order.getId()));
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void deleteById_returnsFalseForMissingId() {
        var repo = new InMemoryCommonOrderRepository();

        assertFalse(repo.deleteById(UUID.randomUUID()));
    }
}
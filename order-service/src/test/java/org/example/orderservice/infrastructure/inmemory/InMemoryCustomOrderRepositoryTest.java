package org.example.orderservice.infrastructure.inmemory;

import org.example.orderservice.domain.order.enums.CustomOrderStatus;
import org.example.orderservice.domain.order.model.CustomCarOrder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCustomOrderRepositoryTest {

    private CustomCarOrder newOrder() {
        return CustomCarOrder.builder()
                .modelId(UUID.randomUUID()).customerId(UUID.randomUUID())
                .selectedPartIds(Map.of())
                .totalPrice(BigDecimal.valueOf(3000000))
                .status(CustomOrderStatus.CREATED).build();
    }

    @Test
    void save_assignsIdOnFirstSave() {
        var repo = new InMemoryCustomOrderRepository();
        CustomCarOrder order = repo.save(newOrder());

        assertNotNull(order.getId());
    }

    @Test
    void save_nullThrowsIllegalArgument() {
        var repo = new InMemoryCustomOrderRepository();
        assertThrows(IllegalArgumentException.class, () -> repo.save(null));
    }

    @Test
    void save_updateExisting_noDuplicate() {
        var repo = new InMemoryCustomOrderRepository();
        CustomCarOrder order = repo.save(newOrder());
        order.setStatus(CustomOrderStatus.APPROVED_BY_WAREHOUSE);
        repo.save(order);
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void findAll_returnsAllSavedOrders() {
        var repo = new InMemoryCustomOrderRepository();
        repo.save(newOrder());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void deleteById_removesExistingOrder() {
        var repo = new InMemoryCustomOrderRepository();
        CustomCarOrder order = repo.save(newOrder());

        assertTrue(repo.deleteById(order.getId()));
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void deleteById_returnsFalseForMissingId() {
        var repo = new InMemoryCustomOrderRepository();

        assertFalse(repo.deleteById(UUID.randomUUID()));
    }
}
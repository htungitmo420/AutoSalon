package org.example.storageservice.presentation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AdminControllerRoleMatrixTest {

    @Test
    void inventoryAndFulfillmentRequireWarehouseAdminOrAdmin() {
        assertEquals("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')",
                AdminInventoryController.class.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')",
                AdminFulfillmentController.class.getAnnotation(PreAuthorize.class).value());
    }

    @Test
    void catalogAndConfiguratorRemainPublic() {
        assertNull(CatalogController.class.getAnnotation(PreAuthorize.class));
        assertNull(ConfiguratorController.class.getAnnotation(PreAuthorize.class));
    }
}

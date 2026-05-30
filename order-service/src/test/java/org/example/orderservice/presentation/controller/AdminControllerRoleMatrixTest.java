package org.example.orderservice.presentation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminControllerRoleMatrixTest {

    @Test
    void orderAndTestDriveAdministrationRequireManagerOrAdmin() {
        assertEquals("hasAnyRole('MANAGER', 'ADMIN')",
                AdminOrderController.class.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasAnyRole('MANAGER', 'ADMIN')",
                AdminTestDriveController.class.getAnnotation(PreAuthorize.class).value());
    }
}

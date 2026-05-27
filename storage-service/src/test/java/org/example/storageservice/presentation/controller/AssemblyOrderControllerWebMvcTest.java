package org.example.storageservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.storageservice.application.dto.request.SaveAssemblyOrderRequest;
import org.example.storageservice.application.dto.response.AssemblyOrderResponse;
import org.example.storageservice.application.service.AssemblyOrderService;
import org.example.storageservice.application.service.InventoryReservationService;
import org.example.storageservice.domain.assembly.enums.AssemblyOrderStatus;
import org.example.storageservice.domain.assembly.enums.SourceOrderType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssemblyOrderController.class)
@AutoConfigureMockMvc
class AssemblyOrderControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssemblyOrderService assemblyOrderService;

    @MockBean
    private InventoryReservationService inventoryReservationService;

    @Test
    void createAssemblyOrder_ReturnsCreated() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID sourceOrderId = UUID.randomUUID();
        SaveAssemblyOrderRequest request = request(sourceOrderId);
        AssemblyOrderResponse response = response(orderId, sourceOrderId, AssemblyOrderStatus.CREATED, null);

        when(assemblyOrderService.createAssemblyOrder(any(SaveAssemblyOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/assembly-orders")
                        .with(jwtForRole("WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.sourceOrderId").value(sourceOrderId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getAssemblyOrder_ReturnsPayload() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID sourceOrderId = UUID.randomUUID();
        AssemblyOrderResponse response = response(orderId, sourceOrderId, AssemblyOrderStatus.CREATED, null);

        when(assemblyOrderService.getAssemblyOrder(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/assembly-orders/{orderId}", orderId)
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()));
    }

    @Test
    void listAssemblyOrders_ReturnsCollection() throws Exception {
        AssemblyOrderResponse first = response(UUID.randomUUID(), UUID.randomUUID(), AssemblyOrderStatus.CREATED, null);
        AssemblyOrderResponse second = response(UUID.randomUUID(), UUID.randomUUID(), AssemblyOrderStatus.FAIL, "No stock");

        when(assemblyOrderService.listAssemblyOrders()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/assembly-orders")
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(first.id().toString()))
                .andExpect(jsonPath("$[1].status").value("FAIL"));
    }

    @Test
    void updateAssemblyOrder_ReturnsUpdatedPayload() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID sourceOrderId = UUID.randomUUID();
        SaveAssemblyOrderRequest request = request(sourceOrderId);
        AssemblyOrderResponse response = response(orderId, sourceOrderId, AssemblyOrderStatus.ASSEMBLED, null);

        when(assemblyOrderService.updateAssemblyOrder(any(UUID.class), any(SaveAssemblyOrderRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/assembly-orders/{orderId}", orderId)
                        .with(jwtForRole("WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSEMBLED"));
    }

    @Test
    void assembleAssemblyOrder_ReturnsAssembled() throws Exception {
        UUID orderId = UUID.randomUUID();
        AssemblyOrderResponse response = response(orderId, UUID.randomUUID(), AssemblyOrderStatus.ASSEMBLED, null);

        when(inventoryReservationService.assemble(any(UUID.class), any(String.class))).thenReturn(response);

        mockMvc.perform(patch("/api/assembly-orders/{orderId}/assemble", orderId)
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSEMBLED"));
    }

    @Test
    void failAssemblyOrder_ReturnsFail() throws Exception {
        UUID orderId = UUID.randomUUID();
        AssemblyOrderResponse response = response(orderId, UUID.randomUUID(), AssemblyOrderStatus.FAIL, "No stock");

        when(inventoryReservationService.failAssembly(any(UUID.class), any(String.class), any(String.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/assembly-orders/{orderId}/fail", orderId)
                        .queryParam("reason", "No stock")
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.failureReason").value("No stock"));
    }

    @Test
    void deleteAssemblyOrder_ReturnsNoContent() throws Exception {
        UUID orderId = UUID.randomUUID();
        doNothing().when(assemblyOrderService).deleteAssemblyOrder(orderId);

        mockMvc.perform(delete("/api/assembly-orders/{orderId}", orderId)
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isNoContent());
    }

    private SaveAssemblyOrderRequest request(UUID sourceOrderId) {
        return new SaveAssemblyOrderRequest(
                sourceOrderId,
                SourceOrderType.CUSTOM,
                null,
                UUID.randomUUID(),
                Map.of("WHEELS", UUID.randomUUID()),
                UUID.randomUUID(),
                AssemblyOrderStatus.CREATED,
                null
        );
    }

    private AssemblyOrderResponse response(UUID orderId, UUID sourceOrderId, AssemblyOrderStatus status,
                                           String failureReason) {
        return new AssemblyOrderResponse(
                orderId,
                sourceOrderId,
                SourceOrderType.CUSTOM,
                null,
                UUID.randomUUID(),
                Map.of("WHEELS", UUID.randomUUID()),
                UUID.randomUUID(),
                status,
                failureReason,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtForRole(String role) {
        return SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}

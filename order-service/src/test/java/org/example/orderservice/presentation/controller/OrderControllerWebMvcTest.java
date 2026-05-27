package org.example.orderservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.application.dto.request.CommonOrderRequest;
import org.example.orderservice.application.dto.request.CustomOrderRequest;
import org.example.orderservice.application.dto.response.CommonOrderResponse;
import org.example.orderservice.application.dto.response.CustomOrderResponse;
import org.example.orderservice.application.service.OrderService;
import org.example.orderservice.domain.order.enums.CommonOrderStatus;
import org.example.orderservice.domain.order.enums.CustomOrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc
class OrderControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void placeCommonOrder_ReturnsCreated() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        CommonOrderRequest request = new CommonOrderRequest(carId, customerId);
        CommonOrderResponse response = new CommonOrderResponse(orderId, carId, customerId,
                CommonOrderStatus.CREATED, Instant.parse("2026-03-22T14:28:30Z"));

        when(orderService.placeCommonOrder(any(CommonOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders/common")
                        .with(jwtForRole("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getCommonOrder_ReturnsPayload() throws Exception {
        UUID orderId = UUID.randomUUID();
        CommonOrderResponse response = new CommonOrderResponse(orderId, UUID.randomUUID(), UUID.randomUUID(),
                CommonOrderStatus.CREATED, Instant.parse("2026-03-22T10:00:00Z"));

        when(orderService.getCommonOrder(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/orders/common/{orderId}", orderId)
                        .with(jwtForRole("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void listCommonOrders_ReturnsCollection() throws Exception {
        CommonOrderResponse first = new CommonOrderResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                CommonOrderStatus.CREATED, Instant.parse("2026-03-22T10:00:00Z"));
        CommonOrderResponse second = new CommonOrderResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                CommonOrderStatus.PAID, Instant.parse("2026-03-22T12:00:00Z"));

        when(orderService.listCommonOrders()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/orders/common")
                        .with(jwtForRole("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(first.id().toString()))
                .andExpect(jsonPath("$[1].status").value("PAID"));
    }

    @Test
    void deleteCommonOrder_ReturnsNoContent() throws Exception {
        UUID orderId = UUID.randomUUID();
        doNothing().when(orderService).deleteCommonOrder(orderId);

        mockMvc.perform(delete("/api/orders/common/{orderId}", orderId)
                        .with(jwtForRole("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void approveCommonOrder_ReturnsUpdatedStatus() throws Exception {
        UUID orderId = UUID.randomUUID();
        CommonOrderResponse response = new CommonOrderResponse(orderId, UUID.randomUUID(), UUID.randomUUID(),
                CommonOrderStatus.APPROVED_BY_MANAGER, Instant.parse("2026-03-22T14:28:30Z"));

        when(orderService.approveCommonOrder(orderId)).thenReturn(response);

        mockMvc.perform(patch("/api/orders/common/{orderId}/approve", orderId)
                        .with(jwtForRole("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("APPROVED_BY_MANAGER"));
    }

    @Test
    void placeCustomOrder_ReturnsCreated() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID wheelPartId = UUID.randomUUID();

        CustomOrderRequest request = new CustomOrderRequest(modelId, customerId, Map.of("WHEELS", wheelPartId));
        CustomOrderResponse response = new CustomOrderResponse(orderId, modelId, customerId,
                Map.of("WHEELS", wheelPartId), BigDecimal.valueOf(2300000), CustomOrderStatus.CREATED,
                Instant.parse("2026-03-22T10:00:00Z"));

        when(orderService.placeCustomOrder(any(CustomOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders/custom")
                        .with(jwtForRole("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getCustomOrder_ReturnsPayload() throws Exception {
        UUID orderId = UUID.randomUUID();
        CustomOrderResponse response = new CustomOrderResponse(orderId, UUID.randomUUID(), UUID.randomUUID(), Map.of(),
                BigDecimal.valueOf(2000000), CustomOrderStatus.PAID, Instant.parse("2026-03-22T10:00:00Z"));

        when(orderService.getCustomOrder(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/orders/custom/{orderId}", orderId)
                        .with(jwtForRole("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void listCustomOrders_ReturnsCollection() throws Exception {
        CustomOrderResponse first = new CustomOrderResponse(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Map.of(),
                BigDecimal.valueOf(2100000), CustomOrderStatus.CREATED,
                Instant.parse("2026-03-22T10:00:00Z"));
        CustomOrderResponse second = new CustomOrderResponse(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Map.of(),
                BigDecimal.valueOf(2500000), CustomOrderStatus.READY_FOR_PICKUP,
                Instant.parse("2026-03-22T12:00:00Z"));

        when(orderService.listCustomOrders()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/orders/custom")
                        .with(jwtForRole("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(first.id().toString()))
                .andExpect(jsonPath("$[1].status").value("READY_FOR_PICKUP"));
    }

    @Test
    void deleteCustomOrder_ReturnsNoContent() throws Exception {
        UUID orderId = UUID.randomUUID();
        doNothing().when(orderService).deleteCustomOrder(orderId);

        mockMvc.perform(delete("/api/orders/custom/{orderId}", orderId)
                        .with(jwtForRole("ADMIN")))
                .andExpect(status().isNoContent());
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtForRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
package org.example.orderservice.presentation.controller;

import org.example.orderservice.application.dto.response.AvailableCarResponse;
import org.example.orderservice.application.exception.StorageServiceUnavailableException;
import org.example.orderservice.application.service.AvailableCarQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvailableCarController.class)
@AutoConfigureMockMvc
class AvailableCarControllerWebMvcTest {

    private static final String CARS_API = "/api/cars";
    private static final String CAR_BY_ID_API = "/api/cars/{carId}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AvailableCarQueryService availableCarQueryService;

    @Test
    void listAvailableCars_ReturnsPayloadForUser() throws Exception {
        UUID carId = UUID.randomUUID();
        AvailableCarResponse car = availableCar(carId);

        when(availableCarQueryService.listAvailableCars()).thenReturn(List.of(car));

        mockMvc.perform(get(CARS_API)
                        .with(jwtForRole("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(carId.toString()))
                .andExpect(jsonPath("$[0].modelName").value("Camry"))
                .andExpect(jsonPath("$[0].brand").value("TOYOTA"));
    }

    @Test
    void getAvailableCar_ReturnsPayloadForManager() throws Exception {
        UUID carId = UUID.randomUUID();
        AvailableCarResponse car = availableCar(carId);

        when(availableCarQueryService.getAvailableCar(carId)).thenReturn(car);

        mockMvc.perform(get(CAR_BY_ID_API, carId)
                        .with(jwtForRole("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(carId.toString()))
                .andExpect(jsonPath("$.modelName").value("Camry"));
    }

    @Test
    void listAvailableCars_AllowsAdmin() throws Exception {
        when(availableCarQueryService.listAvailableCars()).thenReturn(List.of());

        mockMvc.perform(get(CARS_API)
                        .with(jwtForRole("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void listAvailableCars_RejectsAnonymousUser() throws Exception {
        mockMvc.perform(get(CARS_API))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listAvailableCars_ReturnsServiceUnavailableWhenStorageFails() throws Exception {
        when(availableCarQueryService.listAvailableCars())
                .thenThrow(new StorageServiceUnavailableException("Storage service is unavailable"));

        mockMvc.perform(get(CARS_API)
                        .with(jwtForRole("USER")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Storage service unavailable"));
    }

    private AvailableCarResponse availableCar(UUID carId) {
        return new AvailableCarResponse(
                carId,
                UUID.randomUUID(),
                "Camry",
                "TOYOTA",
                "SEDAN",
                "GASOLINE",
                "BLACK",
                BigDecimal.valueOf(2500000)
        );
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtForRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}

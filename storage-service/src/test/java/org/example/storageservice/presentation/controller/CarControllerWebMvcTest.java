package org.example.storageservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.storageservice.application.dto.request.SaveCarRequest;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.application.service.CarService;
import org.example.storageservice.domain.car.enums.Color;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarController.class)
@AutoConfigureMockMvc
class CarControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarService carService;

    @Test
    void createCar_ReturnsCreatedWithPayload() throws Exception {
        UUID carId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();

        SaveCarRequest request = new SaveCarRequest(modelId, Color.BLACK, BigDecimal.valueOf(2000000));
        CarResponse response = new CarResponse(carId, modelId, Color.BLACK, BigDecimal.valueOf(2000000), false);

        when(carService.createCar(any(SaveCarRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/cars")
                        .with(jwtForRole("WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(carId.toString()))
                .andExpect(jsonPath("$.modelId").value(modelId.toString()))
                .andExpect(jsonPath("$.color").value("BLACK"));
    }

    @Test
    void getCar_ReturnsPayload() throws Exception {
        UUID carId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        CarResponse response = new CarResponse(carId, modelId, Color.WHITE, BigDecimal.valueOf(1500000), true);

        when(carService.getCar(carId)).thenReturn(response);

        mockMvc.perform(get("/api/cars/{carId}", carId)
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(carId.toString()))
                .andExpect(jsonPath("$.modelId").value(modelId.toString()))
                .andExpect(jsonPath("$.color").value("WHITE"))
                .andExpect(jsonPath("$.testDrive").value(true));
    }

    @Test
    void listCars_ReturnsCollection() throws Exception {
        UUID modelId = UUID.randomUUID();
        CarResponse first = new CarResponse(UUID.randomUUID(), modelId, Color.BLACK, BigDecimal.valueOf(1000000),
                false);
        CarResponse second = new CarResponse(UUID.randomUUID(), modelId, Color.RED, BigDecimal.valueOf(1200000),
                true);

        when(carService.listCars()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/cars")
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(first.id().toString()))
                .andExpect(jsonPath("$[1].id").value(second.id().toString()))
                .andExpect(jsonPath("$[1].color").value("RED"));
    }

    @Test
    void updateCar_ReturnsUpdatedPayload() throws Exception {
        UUID carId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();

        SaveCarRequest request = new SaveCarRequest(modelId, Color.BLUE, BigDecimal.valueOf(1800000));
        CarResponse response = new CarResponse(carId, modelId, Color.BLUE, BigDecimal.valueOf(1800000), false);

        when(carService.updateCar(eq(carId), any(SaveCarRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/cars/{carId}", carId)
                        .with(jwtForRole("WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(carId.toString()))
                .andExpect(jsonPath("$.color").value("BLUE"))
                .andExpect(jsonPath("$.price").value(1800000));

        verify(carService).updateCar(eq(carId), any(SaveCarRequest.class));
    }

    @Test
    void deleteCar_ReturnsNoContent() throws Exception {
        UUID carId = UUID.randomUUID();
        doNothing().when(carService).deleteCar(carId);

        mockMvc.perform(delete("/api/cars/{carId}", carId)
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void getCar_NotFound_ReturnsConsistentProblemDetail() throws Exception {
        UUID carId = UUID.randomUUID();
        when(carService.getCar(carId)).thenThrow(new EntityNotFoundException("Car not found: " + carId));

        mockMvc.perform(get("/api/cars/{carId}", carId)
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Entity not found"))
                .andExpect(jsonPath("$.detail").value("Car not found: " + carId));
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtForRole(String role) {
        return SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}

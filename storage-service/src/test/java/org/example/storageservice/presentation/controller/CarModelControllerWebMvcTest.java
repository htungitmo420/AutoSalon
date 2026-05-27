package org.example.storageservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.service.CarModelService;
import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.DrivetrainType;
import org.example.storageservice.domain.car.enums.FuelType;
import org.example.storageservice.domain.car.enums.GearBoxType;
import org.example.storageservice.domain.part.enums.PartType;
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
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarModelController.class)
@AutoConfigureMockMvc
class CarModelControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarModelService carModelService;

    @Test
    void createModel_ReturnsCreated() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();

        SaveCarModelRequest request = buildRequest(partId);
        CarModelResponse response = buildResponse(modelId, partId);

        when(carModelService.createCarModel(any(SaveCarModelRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/models")
                        .with(jwtForRole("WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(modelId.toString()))
                .andExpect(jsonPath("$.brand").value("BMW"))
                .andExpect(jsonPath("$.modelName").value("M5"));
    }

    @Test
    void getModel_ReturnsPayload() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();
        CarModelResponse response = buildResponse(modelId, partId);

        when(carModelService.getCarModel(modelId)).thenReturn(response);

        mockMvc.perform(get("/api/models/{modelId}", modelId)
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(modelId.toString()))
                .andExpect(jsonPath("$.basePartIds.WHEELS").value(partId.toString()));
    }

    @Test
    void listModels_ReturnsCollection() throws Exception {
        UUID partId = UUID.randomUUID();
        CarModelResponse first = buildResponse(UUID.randomUUID(), partId);
        CarModelResponse second = buildResponse(UUID.randomUUID(), partId);

        when(carModelService.listCarModels()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/models")
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(first.id().toString()))
                .andExpect(jsonPath("$[1].id").value(second.id().toString()));
    }

    @Test
    void updateModel_ReturnsUpdatedPayload() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();

        SaveCarModelRequest request = buildRequest(partId);
        CarModelResponse response = buildResponse(modelId, partId);

        when(carModelService.updateCarModel(any(UUID.class), any(SaveCarModelRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/models/{modelId}", modelId)
                        .with(jwtForRole("WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(modelId.toString()))
                .andExpect(jsonPath("$.fuelType").value("GASOLINE"));
    }

    @Test
    void deleteModel_ReturnsNoContent() throws Exception {
        UUID modelId = UUID.randomUUID();
        doNothing().when(carModelService).deleteCarModel(modelId);

        mockMvc.perform(delete("/api/models/{modelId}", modelId)
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isNoContent());
    }

    private SaveCarModelRequest buildRequest(UUID partId) {
        return new SaveCarModelRequest("M5", Brand.BMW, BodyType.SEDAN, FuelType.GASOLINE, 500,
                4.4, GearBoxType.AUTOMATIC, DrivetrainType.AWD,
                BigDecimal.valueOf(5000000), Map.of(PartType.WHEELS, partId));
    }

    private CarModelResponse buildResponse(UUID modelId, UUID partId) {
        return new CarModelResponse(modelId, Brand.BMW, "M5", BodyType.SEDAN, FuelType.GASOLINE,
                500, 4.4, GearBoxType.AUTOMATIC, DrivetrainType.AWD,
                BigDecimal.valueOf(5000000), Map.of(PartType.WHEELS, partId));
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtForRole(String role) {
        return SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}

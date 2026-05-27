package org.example.storageservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.storageservice.application.dto.request.CarConfigurationRequest;
import org.example.storageservice.application.dto.response.CarConfigurationResponse;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.service.ConfiguratorService;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfiguratorController.class)
@AutoConfigureMockMvc
class ConfiguratorControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConfiguratorService configuratorService;

    @Test
    void buildConfiguration_ReturnsPayload() throws Exception {
        UUID modelId = UUID.randomUUID();
        UUID selectedPartId = UUID.randomUUID();

        CarConfigurationRequest request = new CarConfigurationRequest(Map.of(PartType.WHEELS, selectedPartId));
        CarConfigurationResponse response = buildResponse(modelId, selectedPartId);

        when(configuratorService.buildConfiguration(any(UUID.class), any(CarConfigurationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/configurator/models/{modelId}", modelId)
                        .with(jwtForRole("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carModel.id").value(modelId.toString()))
                .andExpect(jsonPath("$.selectedParts.WHEELS.id").value(selectedPartId.toString()))
                .andExpect(jsonPath("$.totalPrice").value(5120000));
    }

    private CarConfigurationResponse buildResponse(UUID modelId, UUID selectedPartId) {
        CarModelResponse model = new CarModelResponse(modelId, Brand.BMW, "M5", BodyType.SEDAN,
                FuelType.GASOLINE, 500, 4.4, GearBoxType.AUTOMATIC, DrivetrainType.AWD,
                BigDecimal.valueOf(5000000), Map.of(PartType.WHEELS, selectedPartId));

        PartResponse part = new PartResponse(selectedPartId, PartType.WHEELS, "Sport wheels",
                BigDecimal.valueOf(120000), Set.of(modelId));

        return new CarConfigurationResponse(model, Map.of(PartType.WHEELS, part), BigDecimal.valueOf(5120000));
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtForRole(String role) {
        return SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
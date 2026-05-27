package org.example.storageservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.storageservice.application.dto.request.CarFilterRequest;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.application.service.CatalogService;
import org.example.storageservice.domain.car.enums.BodyType;
import org.example.storageservice.domain.car.enums.Brand;
import org.example.storageservice.domain.car.enums.Color;
import org.example.storageservice.domain.car.enums.DrivetrainType;
import org.example.storageservice.domain.car.enums.FuelType;
import org.example.storageservice.domain.car.enums.GearBoxType;
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
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
@AutoConfigureMockMvc
class CatalogControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogService catalogService;

    @Test
    void getCar_ReturnsPayload() throws Exception {
        UUID carId = UUID.randomUUID();
        CarResponse response = new CarResponse(carId, UUID.randomUUID(), Color.BLACK,
                BigDecimal.valueOf(1800000), true);

        when(catalogService.getCar(carId)).thenReturn(response);

        mockMvc.perform(get("/api/catalog/cars/{carId}", carId)
                        .with(jwtForRole("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(carId.toString()))
                .andExpect(jsonPath("$.color").value("BLACK"));
    }

    @Test
    void filterCars_ReturnsCollection() throws Exception {
        CarFilterRequest filter = new CarFilterRequest(1000000.0, 3000000.0, Brand.BMW, "M5",
                BodyType.SEDAN, FuelType.GASOLINE, 300, 700, 2.0, 5.0,
                GearBoxType.AUTOMATIC, DrivetrainType.AWD, Color.BLACK, Set.of(UUID.randomUUID()));

        CarResponse first = new CarResponse(UUID.randomUUID(), UUID.randomUUID(), Color.BLACK,
                BigDecimal.valueOf(2000000), false);
        CarResponse second = new CarResponse(UUID.randomUUID(), UUID.randomUUID(), Color.BLACK,
                BigDecimal.valueOf(2500000), true);

        when(catalogService.listCars(any(CarFilterRequest.class))).thenReturn(List.of(first, second));

        mockMvc.perform(post("/api/catalog/cars/filter")
                        .with(jwtForRole("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(first.id().toString()))
                .andExpect(jsonPath("$[1].testDrive").value(true));
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtForRole(String role) {
        return SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
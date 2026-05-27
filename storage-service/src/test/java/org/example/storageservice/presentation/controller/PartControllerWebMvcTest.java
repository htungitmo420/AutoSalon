package org.example.storageservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.service.PartService;
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
import java.util.Set;
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

@WebMvcTest(PartController.class)
@AutoConfigureMockMvc
class PartControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PartService partService;

    @Test
    void createPart_ReturnsCreated() throws Exception {
        UUID partId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();

        SavePartRequest request = new SavePartRequest("Sport wheels", PartType.WHEELS,
                BigDecimal.valueOf(120000), Set.of(modelId));
        PartResponse response = new PartResponse(partId, PartType.WHEELS, "Sport wheels",
                BigDecimal.valueOf(120000), Set.of(modelId));

        when(partService.createPart(any(SavePartRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/parts")
                        .with(jwtForRole("WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(partId.toString()))
                .andExpect(jsonPath("$.type").value("WHEELS"));
    }

    @Test
    void getPart_ReturnsPayload() throws Exception {
        UUID partId = UUID.randomUUID();
        PartResponse response = new PartResponse(partId, PartType.OTHER, "Matrix light",
                BigDecimal.valueOf(75000), Set.of());

        when(partService.getPart(partId)).thenReturn(response);

        mockMvc.perform(get("/api/parts/{partId}", partId)
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(partId.toString()))
                .andExpect(jsonPath("$.name").value("Matrix light"));
    }

    @Test
    void listParts_ReturnsCollection() throws Exception {
        PartResponse first = new PartResponse(UUID.randomUUID(), PartType.WHEELS, "Wheels",
                BigDecimal.valueOf(100000), Set.of());
        PartResponse second = new PartResponse(UUID.randomUUID(), PartType.INTERIOR, "Interior",
                BigDecimal.valueOf(220000), Set.of());

        when(partService.listParts()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/parts")
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(first.id().toString()))
                .andExpect(jsonPath("$[1].type").value("INTERIOR"));
    }

    @Test
    void updatePart_ReturnsUpdatedPayload() throws Exception {
        UUID partId = UUID.randomUUID();

        SavePartRequest request = new SavePartRequest("Premium interior", PartType.INTERIOR,
                BigDecimal.valueOf(250000), Set.of());
        PartResponse response = new PartResponse(partId, PartType.INTERIOR, "Premium interior",
                BigDecimal.valueOf(250000), Set.of());

        when(partService.updatePart(any(UUID.class), any(SavePartRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/parts/{partId}", partId)
                        .with(jwtForRole("WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(partId.toString()))
                .andExpect(jsonPath("$.name").value("Premium interior"));
    }

    @Test
    void deletePart_ReturnsNoContent() throws Exception {
        UUID partId = UUID.randomUUID();
        doNothing().when(partService).deletePart(partId);

        mockMvc.perform(delete("/api/parts/{partId}", partId)
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isNoContent());
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtForRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}

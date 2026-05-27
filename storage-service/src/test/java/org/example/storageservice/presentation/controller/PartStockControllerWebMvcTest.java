package org.example.storageservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.storageservice.application.dto.request.SavePartStockRequest;
import org.example.storageservice.application.dto.response.PartStockResponse;
import org.example.storageservice.application.service.PartStockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PartStockController.class)
@AutoConfigureMockMvc
class PartStockControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PartStockService partStockService;

    @Test
    void createPartStock_ReturnsCreated() throws Exception {
        UUID stockId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();
        SavePartStockRequest request = new SavePartStockRequest(partId, 10, 2);
        PartStockResponse response = new PartStockResponse(stockId, partId, 10, 2, 8);

        when(partStockService.createPartStock(any(SavePartStockRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/part-stocks")
                        .with(jwtForRole("WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(stockId.toString()))
                .andExpect(jsonPath("$.availableQuantity").value(8));
    }

    @Test
    void getPartStock_ReturnsPayload() throws Exception {
        UUID stockId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();
        PartStockResponse response = new PartStockResponse(stockId, partId, 10, 2, 8);

        when(partStockService.getPartStock(stockId)).thenReturn(response);

        mockMvc.perform(get("/api/part-stocks/{stockId}", stockId)
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partId").value(partId.toString()));
    }

    @Test
    void listPartStocks_ReturnsCollection() throws Exception {
        PartStockResponse first = new PartStockResponse(UUID.randomUUID(), UUID.randomUUID(), 10, 0, 10);
        PartStockResponse second = new PartStockResponse(UUID.randomUUID(), UUID.randomUUID(), 5, 1, 4);

        when(partStockService.listPartStocks()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/part-stocks")
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(first.id().toString()))
                .andExpect(jsonPath("$[1].availableQuantity").value(4));
    }

    @Test
    void updatePartStock_ReturnsUpdatedPayload() throws Exception {
        UUID stockId = UUID.randomUUID();
        UUID partId = UUID.randomUUID();
        SavePartStockRequest request = new SavePartStockRequest(partId, 20, 3);
        PartStockResponse response = new PartStockResponse(stockId, partId, 20, 3, 17);

        when(partStockService.updatePartStock(any(UUID.class), any(SavePartStockRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/part-stocks/{stockId}", stockId)
                        .with(jwtForRole("WAREHOUSE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservedQuantity").value(3));
    }

    @Test
    void deletePartStock_ReturnsNoContent() throws Exception {
        UUID stockId = UUID.randomUUID();
        doNothing().when(partStockService).deletePartStock(stockId);

        mockMvc.perform(delete("/api/part-stocks/{stockId}", stockId)
                        .with(jwtForRole("WAREHOUSE_ADMIN")))
                .andExpect(status().isNoContent());
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtForRole(String role) {
        return SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}

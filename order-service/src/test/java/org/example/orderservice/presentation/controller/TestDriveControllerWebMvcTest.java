package org.example.orderservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.application.dto.request.BookTestDriveRequest;
import org.example.orderservice.application.dto.response.TestDriveResponse;
import org.example.orderservice.application.service.TestDriveService;
import org.example.orderservice.domain.testdrive.enums.TestDriveStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
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

@WebMvcTest(TestDriveController.class)
@AutoConfigureMockMvc
class TestDriveControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestDriveService testDriveService;

    @Test
    void bookTestDrive_ReturnsCreated() throws Exception {
        UUID testDriveId = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.of(2026, 3, 25, 10, 0);

        BookTestDriveRequest request = new BookTestDriveRequest(carId, customerId, start);
        TestDriveResponse response = new TestDriveResponse(testDriveId, carId, customerId,
                TestDriveStatus.PENDING, start);

        when(testDriveService.bookTestDrive(any(BookTestDriveRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/test-drives")
                        .with(jwtForRole("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testDriveId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getTestDrive_ReturnsPayload() throws Exception {
        UUID testDriveId = UUID.randomUUID();
        TestDriveResponse response = new TestDriveResponse(testDriveId, UUID.randomUUID(), UUID.randomUUID(),
                TestDriveStatus.CONFIRMED, LocalDateTime.of(2026, 3, 25, 11, 30));

        when(testDriveService.getTestDrive(testDriveId)).thenReturn(response);

        mockMvc.perform(get("/api/test-drives/{testDriveId}", testDriveId)
                        .with(jwtForRole("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDriveId.toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void listTestDrives_ReturnsCollection() throws Exception {
        TestDriveResponse first = new TestDriveResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TestDriveStatus.PENDING, LocalDateTime.of(2026, 3, 25, 10, 0));
        TestDriveResponse second = new TestDriveResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TestDriveStatus.CANCELLED, LocalDateTime.of(2026, 3, 25, 12, 0));

        when(testDriveService.listTestDrives()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/test-drives")
                        .with(jwtForRole("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(first.id().toString()))
                .andExpect(jsonPath("$[1].status").value("CANCELLED"));
    }

    @Test
    void deleteTestDrive_ReturnsNoContent() throws Exception {
        UUID testDriveId = UUID.randomUUID();
        doNothing().when(testDriveService).deleteTestDrive(testDriveId);

        mockMvc.perform(delete("/api/test-drives/{testDriveId}", testDriveId)
                        .with(jwtForRole("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void confirmTestDrive_ReturnsUpdatedStatus() throws Exception {
        UUID testDriveId = UUID.randomUUID();
        TestDriveResponse response = new TestDriveResponse(testDriveId, UUID.randomUUID(), UUID.randomUUID(),
                TestDriveStatus.CONFIRMED, LocalDateTime.of(2026, 3, 25, 15, 0));

        when(testDriveService.confirmTestDrive(testDriveId)).thenReturn(response);

        mockMvc.perform(patch("/api/test-drives/{testDriveId}/confirm", testDriveId)
                        .with(jwtForRole("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDriveId.toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void completeTestDrive_ReturnsUpdatedStatus() throws Exception {
        UUID testDriveId = UUID.randomUUID();
        TestDriveResponse response = new TestDriveResponse(testDriveId, UUID.randomUUID(), UUID.randomUUID(),
                TestDriveStatus.COMPLETED, LocalDateTime.of(2026, 3, 25, 16, 0));

        when(testDriveService.completeTestDrive(testDriveId)).thenReturn(response);

        mockMvc.perform(patch("/api/test-drives/{testDriveId}/complete", testDriveId)
                        .with(jwtForRole("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDriveId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void cancelTestDrive_ReturnsUpdatedStatus() throws Exception {
        UUID testDriveId = UUID.randomUUID();
        TestDriveResponse response = new TestDriveResponse(testDriveId, UUID.randomUUID(), UUID.randomUUID(),
                TestDriveStatus.CANCELLED, LocalDateTime.of(2026, 3, 25, 17, 0));

        when(testDriveService.cancelTestDrive(testDriveId)).thenReturn(response);

        mockMvc.perform(patch("/api/test-drives/{testDriveId}/cancel", testDriveId)
                        .with(jwtForRole("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDriveId.toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtForRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
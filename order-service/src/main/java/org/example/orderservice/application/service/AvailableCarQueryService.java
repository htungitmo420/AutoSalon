package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.client.AvailableCarClient;
import org.example.orderservice.application.dto.response.AvailableCarResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvailableCarQueryService {

    private final AvailableCarClient availableCarClient;

    public List<AvailableCarResponse> listAvailableCars() {
        return availableCarClient.listAvailableCars();
    }

    public AvailableCarResponse getAvailableCar(UUID carId) {
        return availableCarClient.getAvailableCar(carId);
    }
}
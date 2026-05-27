package org.example.orderservice.application.client;

import org.example.orderservice.application.dto.response.AvailableCarResponse;

import java.util.List;
import java.util.UUID;

public interface AvailableCarClient {

    List<AvailableCarResponse> listAvailableCars();

    AvailableCarResponse getAvailableCar(UUID carId);
}

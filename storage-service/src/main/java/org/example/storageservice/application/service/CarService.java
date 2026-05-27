package org.example.storageservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.mapper.CarMapper;
import org.example.storageservice.application.dto.request.SaveCarRequest;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.CarRepository;
import org.example.storageservice.domain.car.model.Car;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;

    @Transactional
    public CarResponse createCar(SaveCarRequest request) {
        var model = carModelRepository.findById(request.modelId())
                .orElseThrow(() -> new EntityNotFoundException("Car model not found: " + request.modelId()));

        Car car = CarMapper.INSTANCE.toCar(request);
        car.setModel(model);

        return CarMapper.INSTANCE.toCarResponse(carRepository.save(car));
    }

    @Transactional(readOnly = true)
    public CarResponse getCar(UUID carId) {
        return CarMapper.INSTANCE.toCarResponse(
                carRepository.findById(carId)
                        .orElseThrow(() -> new EntityNotFoundException("Car not found: " + carId))
        );
    }

    @Transactional(readOnly = true)
    public List<CarResponse> listCars() {
        return carRepository.findAll().stream()
                .map(CarMapper.INSTANCE::toCarResponse)
                .toList();
    }

    @Transactional
    public CarResponse updateCar(UUID carId, SaveCarRequest request) {
        Car exist = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + carId));

        var model = carModelRepository.findById(request.modelId())
                .orElseThrow(() -> new EntityNotFoundException("Car model not found: " + request.modelId()));

        Car updated = CarMapper.INSTANCE.toCar(request);
        updated.setId(exist.getId());
        updated.setTestDrive(exist.isTestDrive());
        updated.setModel(model);

        return CarMapper.INSTANCE.toCarResponse(carRepository.save(updated));
    }

    @Transactional
    public void deleteCar(UUID carId) {
        if (!carRepository.deleteById(carId)) {
            throw new EntityNotFoundException("Car not found: " + carId);
        }
    }
}

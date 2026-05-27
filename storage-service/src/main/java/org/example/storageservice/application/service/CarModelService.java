package org.example.storageservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.mapper.CarMapper;
import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.domain.car.model.CarModel;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarModelService {

    private final CarModelRepository carModelRepository;
    private final PartRepository partRepository;

    @Transactional
    public CarModelResponse createCarModel(SaveCarModelRequest request) {
        validatePartsExistence(request.basePartIds().values());

        CarModel model = CarMapper.INSTANCE.toCarModel(request);

        return CarMapper.INSTANCE.toCarModelResponse(carModelRepository.save(model));
    }

    @Transactional(readOnly = true)
    public CarModelResponse getCarModel(UUID modelId) {
        return CarMapper.INSTANCE.toCarModelResponse(
                carModelRepository.findById(modelId)
                        .orElseThrow(() -> new EntityNotFoundException("Car model not found: " + modelId))
        );
    }

    @Transactional(readOnly = true)
    public List<CarModelResponse> listCarModels() {
        return carModelRepository.findAll().stream()
                .map(CarMapper.INSTANCE::toCarModelResponse)
                .toList();
    }

    @Transactional
    public CarModelResponse updateCarModel(UUID modelId, SaveCarModelRequest request) {
        CarModel exist = carModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Car model not found: " + modelId));

        validatePartsExistence(request.basePartIds().values());

        CarModel updated = CarMapper.INSTANCE.toCarModel(request);
        updated.setId(exist.getId());

        return CarMapper.INSTANCE.toCarModelResponse(carModelRepository.save(updated));
    }

    @Transactional
    public void deleteCarModel(UUID modelId) {
        if (!carModelRepository.deleteById(modelId)) {
            throw new EntityNotFoundException("Car model not found: " + modelId);
        }
    }

    private void validatePartsExistence(Collection<UUID> partIds) {
        if (partIds == null || partIds.isEmpty()) return;
        partIds.forEach(id ->
                partRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Part not found: " + id))
        );
    }
}
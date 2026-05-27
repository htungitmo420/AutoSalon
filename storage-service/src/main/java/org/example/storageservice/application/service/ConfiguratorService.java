package org.example.storageservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.mapper.CarMapper;
import org.example.storageservice.application.dto.request.CarConfigurationRequest;
import org.example.storageservice.application.dto.response.CarConfigurationResponse;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.domain.car.model.CarModel;
import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.example.storageservice.domain.exceptions.IncompatibleComponentException;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.domain.part.models.Part;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfiguratorService {

    private final CarModelRepository carModelRepository;
    private final PartRepository partRepository;

    @Transactional(readOnly = true)
    public CarConfigurationResponse buildConfiguration(UUID modelId,
                                                        CarConfigurationRequest request) {
        CarModel model = carModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Car model not found: " + modelId));

        // Determine which "slots" are allowed for this model.
        Set<PartType> allowedSlots = model.getBasePartIds().keySet();

        request.selectedPartIds().keySet().forEach(slot -> {
            if (!allowedSlots.contains(slot)) {
                throw new DomainValidationException(
                        "Unknown part slot for this model: " + slot
                );
            }
        });

        // Build the final configuration
        // Start with base parts from the model, then override with user's selected parts
        Map<PartType, UUID> finalPartIds = new HashMap<>(model.getBasePartIds());
        finalPartIds.putAll(request.selectedPartIds());

        Map<PartType, Part> selectedParts = new HashMap<>();
        finalPartIds.forEach((type, partId) -> {

            Part part = partRepository.findById(partId)
                    .orElseThrow(() -> new EntityNotFoundException("Part not found: " + partId));

            // Part must be compatible with this model
            if (!part.getCompatibleModelIds().contains(model.getId())) {
                throw new IncompatibleComponentException(
                        "Part \"" + part.getName() + "\" is not compatible with model " + modelId
                );
            }

            selectedParts.put(type, part);
        });

        BigDecimal surchargeSum = selectedParts.values().stream()
                .map(Part::getSurcharge)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPrice = model.getBasePrice().add(surchargeSum);

        return CarMapper.INSTANCE.toCarConfigurationResponse(model, selectedParts, totalPrice);
    }
}
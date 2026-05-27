package org.example.storageservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.SaveAssemblyOrderRequest;
import org.example.storageservice.application.dto.response.AssemblyOrderResponse;
import org.example.storageservice.application.mapper.AssemblyOrderMapper;
import org.example.storageservice.application.repository.AssemblyOrderRepository;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.CarRepository;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.domain.assembly.enums.AssemblyOrderStatus;
import org.example.storageservice.domain.assembly.enums.SourceOrderType;
import org.example.storageservice.domain.assembly.model.AssemblyOrder;
import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssemblyOrderService {

    private final AssemblyOrderRepository assemblyOrderRepository;
    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;
    private final PartRepository partRepository;
    private final PartStockService partStockService;

    @Transactional
    public AssemblyOrderResponse createAssemblyOrder(SaveAssemblyOrderRequest request) {
        validateRequest(request);
        validateSourceOrderIsNew(request.sourceOrderId(), null);

        AssemblyOrder order = AssemblyOrderMapper.INSTANCE.toAssemblyOrder(request);
        return AssemblyOrderMapper.INSTANCE.toAssemblyOrderResponse(assemblyOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public AssemblyOrderResponse getAssemblyOrder(UUID assemblyOrderId) {
        return AssemblyOrderMapper.INSTANCE.toAssemblyOrderResponse(findAssemblyOrderOrThrow(assemblyOrderId));
    }

    @Transactional(readOnly = true)
    public List<AssemblyOrderResponse> listAssemblyOrders() {
        return assemblyOrderRepository.findAll().stream()
                .map(AssemblyOrderMapper.INSTANCE::toAssemblyOrderResponse)
                .toList();
    }

    @Transactional
    public AssemblyOrderResponse updateAssemblyOrder(UUID assemblyOrderId, SaveAssemblyOrderRequest request) {
        AssemblyOrder existing = findAssemblyOrderOrThrow(assemblyOrderId);
        validateRequest(request);
        validateSourceOrderIsNew(request.sourceOrderId(), assemblyOrderId);

        AssemblyOrderMapper.INSTANCE.updateAssemblyOrder(request, existing);

        return AssemblyOrderMapper.INSTANCE.toAssemblyOrderResponse(assemblyOrderRepository.save(existing));
    }

    @Transactional
    public void deleteAssemblyOrder(UUID assemblyOrderId) {
        if (!assemblyOrderRepository.deleteById(assemblyOrderId)) {
            throw new EntityNotFoundException("Assembly order not found: " + assemblyOrderId);
        }
    }

    @Transactional
    public AssemblyOrderResponse assemble(UUID assemblyOrderId) {
        AssemblyOrder order = findAssemblyOrderOrThrow(assemblyOrderId);
        if (order.getStatus() != AssemblyOrderStatus.CREATED) {
            throw new DomainValidationException("Can only assemble a CREATED assembly order");
        }
        validateCarIsAvailableIfRequired(order);
        partStockService.reserveParts(order.getRequiredPartIds());
        order.setStatus(AssemblyOrderStatus.ASSEMBLED);
        order.setFailureReason(null);
        return AssemblyOrderMapper.INSTANCE.toAssemblyOrderResponse(assemblyOrderRepository.save(order));
    }

    @Transactional(noRollbackFor = DomainValidationException.class)
    public AssemblyOrderResponse assembleReserved(UUID assemblyOrderId) {
        AssemblyOrder order = findAssemblyOrderOrThrow(assemblyOrderId);
        if (order.getStatus() != AssemblyOrderStatus.CREATED) {
            throw new DomainValidationException("Can only assemble a CREATED assembly order");
        }
        partStockService.consumeReservedParts(order.getRequiredPartIds());
        order.setStatus(AssemblyOrderStatus.ASSEMBLED);
        order.setFailureReason(null);
        return AssemblyOrderMapper.INSTANCE.toAssemblyOrderResponse(assemblyOrderRepository.save(order));
    }

    @Transactional
    public AssemblyOrderResponse failToAssemble(UUID assemblyOrderId, String reason) {
        AssemblyOrder order = findAssemblyOrderOrThrow(assemblyOrderId);
        order.setStatus(AssemblyOrderStatus.FAIL);
        order.setFailureReason(reason);
        return AssemblyOrderMapper.INSTANCE.toAssemblyOrderResponse(assemblyOrderRepository.save(order));
    }

    private AssemblyOrder findAssemblyOrderOrThrow(UUID assemblyOrderId) {
        return assemblyOrderRepository.findById(assemblyOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Assembly order not found: " + assemblyOrderId));
    }

    private void validateRequest(SaveAssemblyOrderRequest request) {
        if (request.sourceOrderId() == null) {
            throw new DomainValidationException("Source order id is required");
        }
        if (request.sourceOrderType() == null) {
            throw new DomainValidationException("Source order type is required");
        }

        if (request.sourceOrderType() == SourceOrderType.COMMON) {
            validateCarExists(request.carId());
        }
        if (request.sourceOrderType() == SourceOrderType.CUSTOM) {
            validateModelExists(request.modelId());
        }

        validateRequiredParts(AssemblyOrderMapper.INSTANCE.copyRequiredPartIds(request.requiredPartIds()));
    }

    private void validateSourceOrderIsNew(UUID sourceOrderId, UUID currentAssemblyOrderId) {
        boolean duplicateExists = assemblyOrderRepository.findAllBySourceOrderId(sourceOrderId).stream()
                .anyMatch(order -> !order.getId().equals(currentAssemblyOrderId));
        if (duplicateExists) {
            throw new DomainValidationException("Assembly order already exists for source order: " + sourceOrderId);
        }
    }

    private void validateCarIsAvailableIfRequired(AssemblyOrder order) {
        if (order.getSourceOrderType() != SourceOrderType.COMMON) {
            return;
        }

        var car = carRepository.findById(order.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + order.getCarId()));
        if (car.isTestDrive()) {
            throw new DomainValidationException("Test drive car cannot be reserved for order: " + order.getCarId());
        }

        boolean alreadyReserved = assemblyOrderRepository.findAllByCarId(order.getCarId()).stream()
                .filter(existing -> !existing.getId().equals(order.getId()))
                .anyMatch(existing -> existing.getStatus() != AssemblyOrderStatus.FAIL);
        if (alreadyReserved) {
            throw new DomainValidationException("Car is not available for order: " + order.getCarId());
        }
    }

    private void validateCarExists(UUID carId) {
        if (carId == null) {
            throw new DomainValidationException("Car id is required for common source orders");
        }
        carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + carId));
    }

    private void validateModelExists(UUID modelId) {
        if (modelId == null) {
            throw new DomainValidationException("Model id is required for custom source orders");
        }
        carModelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Car model not found: " + modelId));
    }

    private void validateRequiredParts(Map<String, UUID> requiredPartIds) {
        requiredPartIds.forEach((slot, partId) -> {
            if (slot == null || slot.isBlank()) {
                throw new DomainValidationException("Part slot is required");
            }
            if (partId == null) {
                throw new DomainValidationException("Part id is required for slot: " + slot);
            }
            partRepository.findById(partId)
                    .orElseThrow(() -> new EntityNotFoundException("Part not found: " + partId));
        });
    }
}

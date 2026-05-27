package org.example.storageservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.SavePartStockRequest;
import org.example.storageservice.application.dto.response.PartStockResponse;
import org.example.storageservice.application.mapper.PartStockMapper;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.application.repository.PartStockRepository;
import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.example.storageservice.domain.stock.model.PartStock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartStockService {

    private final PartStockRepository partStockRepository;
    private final PartRepository partRepository;

    @Transactional
    public PartStockResponse createPartStock(SavePartStockRequest request) {
        validatePartExists(request.partId());
        validateQuantities(request.quantity(), request.reservedQuantity());
        partStockRepository.findByPartId(request.partId()).ifPresent(existing -> {
            throw new DomainValidationException("Part stock already exists for part: " + request.partId());
        });

        PartStock stock = PartStockMapper.INSTANCE.toPartStock(request);

        return PartStockMapper.INSTANCE.toPartStockResponse(partStockRepository.save(stock));
    }

    @Transactional
    public PartStockResponse getPartStock(UUID stockId) {
        return PartStockMapper.INSTANCE.toPartStockResponse(findStockOrThrow(stockId));
    }

    @Transactional
    public List<PartStockResponse> listPartStocks() {
        return partStockRepository.findAll().stream()
                .map(PartStockMapper.INSTANCE::toPartStockResponse)
                .toList();
    }

    @Transactional
    public PartStockResponse updatePartStock(UUID stockId, SavePartStockRequest request) {
        PartStock existing = findStockOrThrow(stockId);
        validatePartExists(request.partId());
        validateQuantities(request.quantity(), request.reservedQuantity());

        partStockRepository.findByPartId(request.partId())
                .filter(stock -> !stock.getId().equals(stockId))
                .ifPresent(stock -> {
                    throw new DomainValidationException("Part stock already exists for part: " + request.partId());
                });

        PartStockMapper.INSTANCE.updatePartStock(request, existing);

        return PartStockMapper.INSTANCE.toPartStockResponse(partStockRepository.save(existing));
    }

    @Transactional
    public void deletePartStock(UUID stockId) {
        if (!partStockRepository.deleteById(stockId)) {
            throw new EntityNotFoundException("Part stock not found: " + stockId);
        }
    }

    @Transactional
    public void reserveParts(Map<String, UUID> requiredPartIds) {
        Map<UUID, Long> requestedQuantities = requestedQuantities(requiredPartIds);
        Map<UUID, PartStock> stocks = lockedStocks(requestedQuantities);

        requestedQuantities.forEach((partId, requestedQuantity) -> {
            PartStock stock = stocks.get(partId);
            if (stock.getAvailableQuantity() < requestedQuantity) {
                throw new DomainValidationException("Not enough stock for part: " + partId);
            }
        });

        requestedQuantities.forEach((partId, requestedQuantity) -> {
            PartStock stock = stocks.get(partId);

            stock.setReservedQuantity(stock.getReservedQuantity() + Math.toIntExact(requestedQuantity));
            partStockRepository.save(stock);
        });
    }

    @Transactional
    public void releaseParts(Map<String, UUID> requiredPartIds) {
        Map<UUID, Long> requestedQuantities = requestedQuantities(requiredPartIds);
        Map<UUID, PartStock> stocks = lockedStocks(requestedQuantities);

        requestedQuantities.forEach((partId, requestedQuantity) -> {
            PartStock stock = stocks.get(partId);
            int amount = Math.toIntExact(requestedQuantity);
            if (stock.getReservedQuantity() < amount) {
                throw new DomainValidationException("Cannot release unreserved stock for part: " + partId);
            }
            stock.setReservedQuantity(stock.getReservedQuantity() - amount);
            partStockRepository.save(stock);
        });
    }

    @Transactional(noRollbackFor = DomainValidationException.class)
    public void consumeReservedParts(Map<String, UUID> requiredPartIds) {
        Map<UUID, Long> requestedQuantities = requestedQuantities(requiredPartIds);
        Map<UUID, PartStock> stocks = lockedStocks(requestedQuantities);

        requestedQuantities.forEach((partId, requestedQuantity) -> {
            PartStock stock = stocks.get(partId);
            int amount = Math.toIntExact(requestedQuantity);
            if (stock.getReservedQuantity() < amount || stock.getQuantity() < amount) {
                throw new DomainValidationException("Cannot consume unreserved stock for part: " + partId);
            }
            stock.setReservedQuantity(stock.getReservedQuantity() - amount);
            stock.setQuantity(stock.getQuantity() - amount);
            partStockRepository.save(stock);
        });
    }

    private PartStock findStockOrThrow(UUID stockId) {
        return partStockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException("Part stock not found: " + stockId));
    }

    private void validatePartExists(UUID partId) {
        if (partId == null) {
            throw new DomainValidationException("Part id is required");
        }
        partRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException("Part not found: " + partId));
    }

    private void validateQuantities(int quantity, int reservedQuantity) {
        if (quantity < 0 || reservedQuantity < 0) {
            throw new DomainValidationException("Stock quantities must not be negative");
        }
        if (reservedQuantity > quantity) {
            throw new DomainValidationException("Reserved quantity cannot exceed quantity");
        }
    }

    private Map<String, UUID> normalizeRequiredParts(Map<String, UUID> requiredPartIds) {
        return requiredPartIds == null ? Map.of() : Map.copyOf(requiredPartIds);
    }

    private Map<UUID, Long> requestedQuantities(Map<String, UUID> requiredPartIds) {
        return normalizeRequiredParts(requiredPartIds).values().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private Map<UUID, PartStock> lockedStocks(Map<UUID, Long> requestedQuantities) {
        return requestedQuantities.keySet().stream()
                .collect(Collectors.toMap(Function.identity(), partId -> partStockRepository.findByPartIdForUpdate(partId)
                        .orElseThrow(() -> new DomainValidationException("Part stock not found for part: " + partId))));
    }
}

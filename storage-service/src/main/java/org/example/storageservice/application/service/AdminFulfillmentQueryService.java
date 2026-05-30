package org.example.storageservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.response.AssemblyOrderResponse;
import org.example.storageservice.application.dto.response.PageResponse;
import org.example.storageservice.application.mapper.AssemblyOrderMapper;
import org.example.storageservice.application.repository.AssemblyOrderRepository;
import org.example.storageservice.domain.assembly.enums.AssemblyOrderStatus;
import org.example.storageservice.domain.assembly.enums.SourceOrderType;
import org.example.storageservice.domain.assembly.model.AssemblyOrder;
import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminFulfillmentQueryService {

    private final AssemblyOrderRepository assemblyOrderRepository;

    @Transactional(readOnly = true)
    public PageResponse<AssemblyOrderResponse> listAssemblyOrders(AssemblyOrderStatus status, SourceOrderType sourceType,
                                                                  UUID sourceOrderId, int page, int size, String sortBy,
                                                                  String sortDirection) {
        Stream<AssemblyOrder> orders = assemblyOrderRepository.findAll().stream()
                .filter(order -> status == null || status == order.getStatus())
                .filter(order -> sourceType == null || sourceType == order.getSourceOrderType())
                .filter(order -> sourceOrderId == null || sourceOrderId.equals(order.getSourceOrderId()));
        return PageSupport.page(orders, page, size, sortBy, sortDirection, comparator(sortBy),
                AssemblyOrderMapper.INSTANCE::toAssemblyOrderResponse);
    }

    private Comparator<AssemblyOrder> comparator(String sortBy) {
        return switch (sortBy) {
            case "createdAt" -> Comparator.comparing(AssemblyOrder::getCreatedAt);
            case "status" -> Comparator.comparing(AssemblyOrder::getStatus);
            case "id" -> Comparator.comparing(AssemblyOrder::getId);
            default -> throw new DomainValidationException("Unsupported sort field for assembly orders: " + sortBy);
        };
    }
}

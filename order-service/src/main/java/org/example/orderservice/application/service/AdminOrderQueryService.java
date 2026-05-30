package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.dto.response.CommonOrderResponse;
import org.example.orderservice.application.dto.response.CustomOrderResponse;
import org.example.orderservice.application.dto.response.PageResponse;
import org.example.orderservice.application.mapper.OrderMapper;
import org.example.orderservice.application.repository.CommonOrderRepository;
import org.example.orderservice.application.repository.CustomOrderRepository;
import org.example.orderservice.domain.exceptions.DomainValidationException;
import org.example.orderservice.domain.order.enums.CommonOrderStatus;
import org.example.orderservice.domain.order.enums.CustomOrderStatus;
import org.example.orderservice.domain.order.model.CommonCarOrder;
import org.example.orderservice.domain.order.model.CustomCarOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminOrderQueryService {

    private final CommonOrderRepository commonOrderRepository;
    private final CustomOrderRepository customOrderRepository;

    @Transactional(readOnly = true)
    public PageResponse<CommonOrderResponse> listCommonOrders(UUID customerId, CommonOrderStatus status, int page,
                                                              int size, String sortBy, String sortDirection) {
        Stream<CommonCarOrder> orders = commonOrderRepository.findAll().stream()
                .filter(order -> customerId == null || customerId.equals(order.getCustomerId()))
                .filter(order -> status == null || status == order.getStatus());
        return PageSupport.page(orders, page, size, sortBy, sortDirection, commonComparator(sortBy),
                OrderMapper.INSTANCE::toCommonOrderResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomOrderResponse> listCustomOrders(UUID customerId, CustomOrderStatus status, int page,
                                                              int size, String sortBy, String sortDirection) {
        Stream<CustomCarOrder> orders = customOrderRepository.findAll().stream()
                .filter(order -> customerId == null || customerId.equals(order.getCustomerId()))
                .filter(order -> status == null || status == order.getStatus());
        return PageSupport.page(orders, page, size, sortBy, sortDirection, customComparator(sortBy),
                OrderMapper.INSTANCE::toCustomOrderResponse);
    }

    private Comparator<CommonCarOrder> commonComparator(String sortBy) {
        return switch (sortBy) {
            case "createdAt" -> Comparator.comparing(CommonCarOrder::getCreatedAt);
            case "status" -> Comparator.comparing(CommonCarOrder::getStatus);
            case "id" -> Comparator.comparing(CommonCarOrder::getId);
            default -> throw unsupportedSort(sortBy);
        };
    }

    private Comparator<CustomCarOrder> customComparator(String sortBy) {
        return switch (sortBy) {
            case "createdAt" -> Comparator.comparing(CustomCarOrder::getCreatedAt);
            case "status" -> Comparator.comparing(CustomCarOrder::getStatus);
            case "totalPrice" -> Comparator.comparing(
                    CustomCarOrder::getTotalPrice, Comparator.nullsLast(Comparator.naturalOrder()));
            case "id" -> Comparator.comparing(CustomCarOrder::getId);
            default -> throw unsupportedSort(sortBy);
        };
    }

    private DomainValidationException unsupportedSort(String sortBy) {
        return new DomainValidationException("Unsupported sort field for orders: " + sortBy);
    }
}

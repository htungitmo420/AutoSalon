package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.client.InventoryReservationClient;
import org.example.orderservice.application.dto.request.CommonOrderRequest;
import org.example.orderservice.application.dto.request.CustomOrderRequest;
import org.example.orderservice.application.dto.response.CommonOrderResponse;
import org.example.orderservice.application.dto.response.CustomOrderResponse;
import org.example.orderservice.application.dto.response.InventoryReservationResponse;
import org.example.orderservice.application.mapper.OrderMapper;
import org.example.orderservice.application.port.out.CurrentUserProvider;
import org.example.orderservice.application.port.out.OrderWorkflowEventPublisher;
import org.example.orderservice.application.repository.CommonOrderRepository;
import org.example.orderservice.application.repository.CustomOrderRepository;
import org.example.orderservice.domain.exceptions.DomainValidationException;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;
import org.example.orderservice.domain.order.enums.CommonOrderStatus;
import org.example.orderservice.domain.order.enums.CustomOrderStatus;
import org.example.orderservice.domain.order.model.CommonCarOrder;
import org.example.orderservice.domain.order.model.CustomCarOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CommonOrderRepository commonOrderRepository;
    private final CustomOrderRepository customOrderRepository;
    private final CurrentUserProvider currentUserProvider;
    private final InventoryReservationClient inventoryReservationClient;
    private final OrderWorkflowEventPublisher eventPublisher;

    @Value("${order.reservation.ttl-minutes:15}")
    private long reservationTtlMinutes = 15;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CommonOrderResponse placeCommonOrder(CommonOrderRequest request) {
        CommonCarOrder order = OrderMapper.INSTANCE.toCommonCarOrder(request, currentUserProvider.getCurrentUserId());
        order = commonOrderRepository.save(order);
        try {
            InventoryReservationResponse reservation = inventoryReservationClient.reserveStockCar(
                    order.getId(), order.getCarId(), reservationExpiry());
            order.setReservationId(reservation.reservationId());
            order.setStatus(CommonOrderStatus.WAITING_FOR_PAYMENT);
            CommonCarOrder saved = commonOrderRepository.save(order);
            eventPublisher.publishCommonOrderAwaitingPayment(
                    saved.getId(), saved.getReservationId(), reservation.totalPrice());
            return OrderMapper.INSTANCE.toCommonOrderResponse(saved);
        } catch (DomainValidationException ex) {
            order.setStatus(CommonOrderStatus.REJECTED);
            return OrderMapper.INSTANCE.toCommonOrderResponse(commonOrderRepository.save(order));
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityEvaluator.isCommonOrderOwner(#orderId, authentication)")
    public CommonOrderResponse getCommonOrder(UUID orderId) {
        return OrderMapper.INSTANCE.toCommonOrderResponse(findCommonOrderOrThrow(orderId));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<CommonOrderResponse> listCommonOrders() {
        List<CommonCarOrder> orders = currentUserProvider.hasElevatedReadAccess()
                ? commonOrderRepository.findAll()
                : commonOrderRepository.findAllByCustomerId(currentUserProvider.getCurrentUserId());
        return orders.stream().map(OrderMapper.INSTANCE::toCommonOrderResponse).toList();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityEvaluator.isCommonOrderOwner(#orderId, authentication)")
    public void deleteCommonOrder(UUID orderId) {
        if (!commonOrderRepository.deleteById(orderId)) {
            throw new EntityNotFoundException("Common order not found: " + orderId);
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CustomOrderResponse placeCustomOrder(CustomOrderRequest request) {
        Map<String, UUID> selectedPartIds = normalizeSelectedPartIds(request.selectedPartIds());
        CustomCarOrder order = OrderMapper.INSTANCE.toCustomCarOrder(
                request, request.modelId(), currentUserProvider.getCurrentUserId(), selectedPartIds, null);
        order = customOrderRepository.save(order);
        try {
            InventoryReservationResponse reservation = inventoryReservationClient.reserveConfiguration(
                    order.getId(), order.getModelId(), selectedPartIds, reservationExpiry());
            order.setReservationId(reservation.reservationId());
            order.setTotalPrice(reservation.totalPrice());
            order.setStatus(CustomOrderStatus.WAITING_FOR_PAYMENT);
            CustomCarOrder saved = customOrderRepository.save(order);
            eventPublisher.publishCustomOrderAwaitingPayment(
                    saved.getId(), saved.getReservationId(), saved.getTotalPrice());
            return OrderMapper.INSTANCE.toCustomOrderResponse(saved);
        } catch (DomainValidationException ex) {
            order.setStatus(CustomOrderStatus.REJECTED);
            return OrderMapper.INSTANCE.toCustomOrderResponse(customOrderRepository.save(order));
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityEvaluator.isCustomOrderOwner(#orderId, authentication)")
    public CustomOrderResponse getCustomOrder(UUID orderId) {
        return OrderMapper.INSTANCE.toCustomOrderResponse(findCustomOrderOrThrow(orderId));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<CustomOrderResponse> listCustomOrders() {
        List<CustomCarOrder> orders = currentUserProvider.hasElevatedReadAccess()
                ? customOrderRepository.findAll()
                : customOrderRepository.findAllByCustomerId(currentUserProvider.getCurrentUserId());
        return orders.stream().map(OrderMapper.INSTANCE::toCustomOrderResponse).toList();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN')")
    public void deleteCustomOrder(UUID orderId) {
        if (!customOrderRepository.deleteById(orderId)) {
            throw new EntityNotFoundException("Custom order not found: " + orderId);
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CommonOrderResponse approveCommonOrder(UUID orderId) {
        throw new DomainValidationException("Manual approval is retired; checkout reserves inventory before payment");
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CommonOrderResponse requestCommonOrderPayment(UUID orderId) {
        throw new DomainValidationException("Payment is requested automatically after reservation");
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CommonOrderResponse markCommonOrderPaid(UUID orderId) {
        CommonCarOrder order = findCommonOrderOrThrow(orderId);
        validateCommonOrderStatus(order, CommonOrderStatus.WAITING_FOR_PAYMENT,
                "Can only mark as paid when WAITING_FOR_PAYMENT");
        validateReservationId(order.getReservationId());
        inventoryReservationClient.confirmReservation(order.getId(), order.getReservationId());
        order.setStatus(CommonOrderStatus.PAID);
        return OrderMapper.INSTANCE.toCommonOrderResponse(commonOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CommonOrderResponse markCommonOrderReadyForPickup(UUID orderId) {
        return transitionCommonOrder(orderId, CommonOrderStatus.PAID, CommonOrderStatus.READY_FOR_PICKUP,
                "Can only mark as READY_FOR_PICKUP when PAID");
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CommonOrderResponse completeCommonOrder(UUID orderId) {
        return transitionCommonOrder(orderId, CommonOrderStatus.READY_FOR_PICKUP, CommonOrderStatus.COMPLETED,
                "Can only complete when READY_FOR_PICKUP");
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityEvaluator.isCommonOrderOwner(#orderId, authentication)")
    public CommonOrderResponse cancelCommonOrder(UUID orderId) {
        CommonCarOrder order = findCommonOrderOrThrow(orderId);
        if (order.getStatus() != CommonOrderStatus.WAITING_FOR_PAYMENT
                && order.getStatus() != CommonOrderStatus.PENDING_RESERVATION
                && order.getStatus() != CommonOrderStatus.CREATED
                && order.getStatus() != CommonOrderStatus.APPROVED_BY_MANAGER) {
            throw new DomainValidationException("Cannot cancel an order with status: " + order.getStatus());
        }
        order.setStatus(CommonOrderStatus.CANCELLED);
        CommonCarOrder saved = commonOrderRepository.save(order);
        if (saved.getReservationId() != null) {
            eventPublisher.publishCommonOrderCancelled(saved.getId(), saved.getReservationId());
        }
        return OrderMapper.INSTANCE.toCommonOrderResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public CustomOrderResponse approveCustomOrder(UUID orderId) {
        throw new DomainValidationException("Manual approval is retired; checkout reserves inventory before payment");
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomOrderResponse requestCustomOrderPayment(UUID orderId) {
        throw new DomainValidationException("Payment is requested automatically after reservation");
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomOrderResponse markCustomOrderPaid(UUID orderId) {
        CustomCarOrder order = findCustomOrderOrThrow(orderId);
        validateCustomOrderStatus(order, CustomOrderStatus.WAITING_FOR_PAYMENT,
                "Can only mark as paid when WAITING_FOR_PAYMENT");
        validateReservationId(order.getReservationId());
        inventoryReservationClient.confirmReservation(order.getId(), order.getReservationId());
        order.setStatus(CustomOrderStatus.PAID);
        customOrderRepository.save(order);

        // A confirmed custom reservation creates an assembly work item in storage.
        order.setStatus(CustomOrderStatus.ASSEMBLING);
        return OrderMapper.INSTANCE.toCustomOrderResponse(customOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'MANAGER', 'ADMIN')")
    public CustomOrderResponse markCustomOrderWaitingForDelivery(UUID orderId) {
        return transitionCustomOrder(orderId, CustomOrderStatus.PAID, CustomOrderStatus.WAITING_FOR_DELIVERY,
                "Can only set WAITING_FOR_DELIVERY when PAID");
    }

    @Transactional
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'MANAGER', 'ADMIN')")
    public CustomOrderResponse markCustomOrderReadyForPickup(UUID orderId) {
        return transitionCustomOrder(orderId, CustomOrderStatus.WAITING_FOR_DELIVERY,
                CustomOrderStatus.READY_FOR_PICKUP,
                "Can only mark as READY_FOR_PICKUP when WAITING_FOR_DELIVERY");
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomOrderResponse completeCustomOrder(UUID orderId) {
        return transitionCustomOrder(orderId, CustomOrderStatus.READY_FOR_PICKUP, CustomOrderStatus.COMPLETED,
                "Can only complete when READY_FOR_PICKUP");
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityEvaluator.isCustomOrderOwner(#orderId, authentication)")
    public CustomOrderResponse cancelCustomOrder(UUID orderId) {
        CustomCarOrder order = findCustomOrderOrThrow(orderId);
        if (order.getStatus() != CustomOrderStatus.WAITING_FOR_PAYMENT
                && order.getStatus() != CustomOrderStatus.PENDING_RESERVATION
                && order.getStatus() != CustomOrderStatus.CREATED
                && order.getStatus() != CustomOrderStatus.APPROVED_BY_WAREHOUSE) {
            throw new DomainValidationException("Cannot cancel an order with status: " + order.getStatus());
        }
        order.setStatus(CustomOrderStatus.CANCELLED);
        CustomCarOrder saved = customOrderRepository.save(order);
        if (saved.getReservationId() != null) {
            eventPublisher.publishCustomOrderCancelled(saved.getId(), saved.getReservationId());
        }
        return OrderMapper.INSTANCE.toCustomOrderResponse(saved);
    }

    @Transactional
    public void handleCommonReservationExpired(UUID orderId) {
        CommonCarOrder order = findCommonOrderOrThrow(orderId);
        if (order.getStatus() == CommonOrderStatus.WAITING_FOR_PAYMENT) {
            order.setStatus(CommonOrderStatus.CANCELLED);
            commonOrderRepository.save(order);
        }
    }

    @Transactional
    public void handleCustomReservationExpired(UUID orderId) {
        CustomCarOrder order = findCustomOrderOrThrow(orderId);
        if (order.getStatus() == CustomOrderStatus.WAITING_FOR_PAYMENT) {
            order.setStatus(CustomOrderStatus.CANCELLED);
            customOrderRepository.save(order);
        }
    }

    @Transactional
    public void handleAssemblyCompleted(UUID orderId) {
        CustomCarOrder order = findCustomOrderOrThrow(orderId);
        if (order.getStatus() == CustomOrderStatus.ASSEMBLING) {
            order.setStatus(CustomOrderStatus.READY_FOR_PICKUP);
            customOrderRepository.save(order);
        }
    }

    @Transactional
    public void handleAssemblyFailed(UUID orderId) {
        CustomCarOrder order = findCustomOrderOrThrow(orderId);
        if (order.getStatus() == CustomOrderStatus.ASSEMBLING) {
            order.setStatus(CustomOrderStatus.REFUND_REQUIRED);
            customOrderRepository.save(order);
        }
    }

    // Compatibility handlers for legacy events emitted before reservation v1 is fully removed.
    @Transactional
    public void handleCommonStorageApproved(UUID orderId) {
        CommonCarOrder order = findCommonOrderOrThrow(orderId);
        if (order.getStatus() == CommonOrderStatus.PAID) {
            order.setStatus(CommonOrderStatus.READY_FOR_PICKUP);
            commonOrderRepository.save(order);
        }
    }

    @Transactional
    public void handleCustomStorageApproved(UUID orderId) {
        CustomCarOrder order = findCustomOrderOrThrow(orderId);
        if (order.getStatus() == CustomOrderStatus.PAID) {
            order.setStatus(CustomOrderStatus.READY_FOR_PICKUP);
            customOrderRepository.save(order);
        }
    }

    @Transactional
    public void handleCommonStorageRejected(UUID orderId) {
        CommonCarOrder order = findCommonOrderOrThrow(orderId);
        if (order.getStatus() == CommonOrderStatus.PAID) {
            order.setStatus(CommonOrderStatus.REFUND_REQUIRED);
            commonOrderRepository.save(order);
        }
    }

    @Transactional
    public void handleCustomStorageRejected(UUID orderId) {
        CustomCarOrder order = findCustomOrderOrThrow(orderId);
        if (order.getStatus() == CustomOrderStatus.PAID) {
            order.setStatus(CustomOrderStatus.REFUND_REQUIRED);
            customOrderRepository.save(order);
        }
    }

    private Instant reservationExpiry() {
        return Instant.now().plus(reservationTtlMinutes, ChronoUnit.MINUTES);
    }

    private void validateReservationId(UUID reservationId) {
        if (reservationId == null) {
            throw new DomainValidationException("Order has no held reservation");
        }
    }

    private CommonOrderResponse transitionCommonOrder(UUID orderId, CommonOrderStatus required,
                                                      CommonOrderStatus next, String errorMessage) {
        CommonCarOrder order = findCommonOrderOrThrow(orderId);
        validateCommonOrderStatus(order, required, errorMessage);
        order.setStatus(next);
        return OrderMapper.INSTANCE.toCommonOrderResponse(commonOrderRepository.save(order));
    }

    private CommonCarOrder findCommonOrderOrThrow(UUID orderId) {
        return commonOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Common order not found: " + orderId));
    }

    private CustomOrderResponse transitionCustomOrder(UUID orderId, CustomOrderStatus required,
                                                      CustomOrderStatus next, String errorMessage) {
        CustomCarOrder order = findCustomOrderOrThrow(orderId);
        validateCustomOrderStatus(order, required, errorMessage);
        order.setStatus(next);
        return OrderMapper.INSTANCE.toCustomOrderResponse(customOrderRepository.save(order));
    }

    private CustomCarOrder findCustomOrderOrThrow(UUID orderId) {
        return customOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Custom order not found: " + orderId));
    }

    private Map<String, UUID> normalizeSelectedPartIds(Map<String, UUID> selectedPartIds) {
        return selectedPartIds == null ? Map.of() : Map.copyOf(selectedPartIds);
    }

    private void validateCommonOrderStatus(CommonCarOrder order, CommonOrderStatus required, String errorMessage) {
        if (order.getStatus() != required) {
            throw new DomainValidationException(errorMessage + ", current status is " + order.getStatus());
        }
    }

    private void validateCustomOrderStatus(CustomCarOrder order, CustomOrderStatus required, String errorMessage) {
        if (order.getStatus() != required) {
            throw new DomainValidationException(errorMessage + ", current status is " + order.getStatus());
        }
    }
}

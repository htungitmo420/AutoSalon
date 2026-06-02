package org.example.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.client.CartSnapshotClient;
import org.example.orderservice.application.client.InventoryReservationClient;
import org.example.orderservice.application.dto.request.CartCheckoutRequest;
import org.example.orderservice.application.dto.request.CommonOrderRequest;
import org.example.orderservice.application.dto.request.CustomOrderRequest;
import org.example.orderservice.application.dto.response.CartCheckoutResponse;
import org.example.orderservice.application.dto.response.CartSnapshotResponse;
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
import org.example.commoncontracts.payment.PaymentPurpose;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CommonOrderRepository commonOrderRepository;
    private final CustomOrderRepository customOrderRepository;
    private final CurrentUserProvider currentUserProvider;
    private final InventoryReservationClient inventoryReservationClient;
    private final CartSnapshotClient cartSnapshotClient;
    private final OrderWorkflowEventPublisher eventPublisher;

    @Value("${order.reservation.ttl-minutes:15}")
    private long reservationTtlMinutes = 15;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CommonOrderResponse placeCommonOrder(CommonOrderRequest request) {
        return placeCommonOrder(request, null, null);
    }

    private CommonOrderResponse placeCommonOrder(CommonOrderRequest request, UUID cartId, BigDecimal quotedPrice) {
        CommonCarOrder order = OrderMapper.INSTANCE.toCommonCarOrder(request, currentUserProvider.getCurrentUserId());
        order.setCartId(cartId);
        order = commonOrderRepository.save(order);
        try {
            InventoryReservationResponse reservation = inventoryReservationClient.reserveStockCar(
                    order.getId(), order.getCarId(), reservationExpiry());
            validateQuotedPrice(order.getId(), reservation, quotedPrice);
            order.setReservationId(reservation.reservationId());
            order.setTotalPrice(reservation.totalPrice());
            order.setStatus(CommonOrderStatus.WAITING_FOR_PAYMENT);
            CommonCarOrder saved = commonOrderRepository.save(order);
            eventPublisher.publishCommonOrderAwaitingPayment(
                    saved.getId(), saved.getCustomerId(), saved.getReservationId(), saved.getTotalPrice());
            return OrderMapper.INSTANCE.toCommonOrderResponse(saved);
        } catch (DomainValidationException ex) {
            if (cartId != null) {
                throw ex;
            }
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
        return placeCustomOrder(request, null, null);
    }

    private CustomOrderResponse placeCustomOrder(CustomOrderRequest request, UUID cartId, BigDecimal quotedPrice) {
        Map<String, UUID> selectedPartIds = normalizeSelectedPartIds(request.selectedPartIds());
        CustomCarOrder order = OrderMapper.INSTANCE.toCustomCarOrder(
                request, request.modelId(), currentUserProvider.getCurrentUserId(), selectedPartIds, null);
        order.setCartId(cartId);
        order = customOrderRepository.save(order);
        try {
            InventoryReservationResponse reservation = inventoryReservationClient.reserveConfiguration(
                    order.getId(), order.getModelId(), selectedPartIds, reservationExpiry());
            validateQuotedPrice(order.getId(), reservation, quotedPrice);
            order.setReservationId(reservation.reservationId());
            order.setTotalPrice(reservation.totalPrice());
            order.setStatus(CustomOrderStatus.WAITING_FOR_PAYMENT);
            CustomCarOrder saved = customOrderRepository.save(order);
            eventPublisher.publishCustomOrderAwaitingPayment(
                    saved.getId(), saved.getCustomerId(), saved.getReservationId(), saved.getTotalPrice());
            return OrderMapper.INSTANCE.toCustomOrderResponse(saved);
        } catch (DomainValidationException ex) {
            if (cartId != null) {
                throw ex;
            }
            order.setStatus(CustomOrderStatus.REJECTED);
            return OrderMapper.INSTANCE.toCustomOrderResponse(customOrderRepository.save(order));
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CartCheckoutResponse checkoutCart(CartCheckoutRequest request) {
        if (request.cartId() == null) {
            throw new DomainValidationException("cartId is required");
        }
        UUID customerId = currentUserProvider.getCurrentUserId();
        CartSnapshotResponse snapshot = cartSnapshotClient.lockCart(request.cartId(), customerId);
        try {
            Optional<CartCheckoutResponse> existingCheckout = findExistingCheckout(request.cartId());
            if (existingCheckout.isPresent()) {
                cartSnapshotClient.markCheckedOut(request.cartId(), customerId);
                return existingCheckout.get();
            }
            validateSnapshotOwner(snapshot, customerId);
            CartCheckoutResponse response = switch (snapshot.itemType()) {
                case "STOCK_CAR" -> CartCheckoutResponse.stockCar(snapshot.cartId(), placeCommonOrder(
                        new CommonOrderRequest(snapshot.carId(), customerId),
                        snapshot.cartId(),
                        snapshot.quotedPrice()));
                case "CONFIGURED_CAR" -> CartCheckoutResponse.configuredCar(snapshot.cartId(), placeCustomOrder(
                        new CustomOrderRequest(snapshot.modelId(), customerId, snapshot.selectedPartIds()),
                        snapshot.cartId(),
                        snapshot.quotedPrice()));
                default -> throw new DomainValidationException("Unsupported cart item type: " + snapshot.itemType());
            };
            cartSnapshotClient.markCheckedOut(snapshot.cartId(), customerId);
            return response;
        } catch (RuntimeException exception) {
            releaseCartAfterFailure(request.cartId(), customerId, exception);
            throw exception;
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
        throw new DomainValidationException("Manual mark-paid is retired; payment webhook updates the order");
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
        throw new DomainValidationException("Manual mark-paid is retired; payment webhook updates the order");
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
            CommonCarOrder saved = commonOrderRepository.save(order);
            if (saved.getReservationId() != null) {
                eventPublisher.publishCommonOrderCancelled(saved.getId(), saved.getReservationId());
            }
        }
    }

    @Transactional
    public void handleCustomReservationExpired(UUID orderId) {
        CustomCarOrder order = findCustomOrderOrThrow(orderId);
        if (order.getStatus() == CustomOrderStatus.WAITING_FOR_PAYMENT) {
            order.setStatus(CustomOrderStatus.CANCELLED);
            CustomCarOrder saved = customOrderRepository.save(order);
            if (saved.getReservationId() != null) {
                eventPublisher.publishCustomOrderCancelled(saved.getId(), saved.getReservationId());
            }
        }
    }

    @Transactional
    public void handleAssemblyCompleted(UUID orderId) {
        CustomCarOrder order = findCustomOrderOrThrow(orderId);
        if (order.getStatus() == CustomOrderStatus.ASSEMBLING) {
            order.setStatus(isFullyPaid(order.getPaidAmount(), order.getTotalPrice())
                    ? CustomOrderStatus.READY_FOR_PICKUP
                    : CustomOrderStatus.WAITING_FOR_BALANCE);
            customOrderRepository.save(order);
        }
    }

    @Transactional
    public void handleCommonPaymentSucceeded(UUID orderId, PaymentPurpose purpose, BigDecimal amount) {
        CommonCarOrder order = findCommonOrderOrThrow(orderId);
        if (order.getStatus() != CommonOrderStatus.WAITING_FOR_PAYMENT
                && order.getStatus() != CommonOrderStatus.DEPOSIT_PAID) {
            return;
        }
        BigDecimal paidAmount = addPayment(order.getPaidAmount(), amount, order.getTotalPrice());
        if (order.getStatus() == CommonOrderStatus.WAITING_FOR_PAYMENT) {
            confirmHeldReservation(order.getId(), order.getReservationId());
        }
        order.setPaidAmount(paidAmount);
        order.setStatus(isFullyPaid(paidAmount, order.getTotalPrice())
                ? CommonOrderStatus.PAID
                : CommonOrderStatus.DEPOSIT_PAID);
        commonOrderRepository.save(order);
    }

    @Transactional
    public void handleCustomPaymentSucceeded(UUID orderId, PaymentPurpose purpose, BigDecimal amount) {
        CustomCarOrder order = findCustomOrderOrThrow(orderId);
        if (order.getStatus() != CustomOrderStatus.WAITING_FOR_PAYMENT
                && order.getStatus() != CustomOrderStatus.ASSEMBLING
                && order.getStatus() != CustomOrderStatus.WAITING_FOR_BALANCE) {
            return;
        }
        BigDecimal paidAmount = addPayment(order.getPaidAmount(), amount, order.getTotalPrice());
        if (order.getStatus() == CustomOrderStatus.WAITING_FOR_PAYMENT) {
            confirmHeldReservation(order.getId(), order.getReservationId());
            order.setStatus(CustomOrderStatus.ASSEMBLING);
        } else if (order.getStatus() == CustomOrderStatus.WAITING_FOR_BALANCE
                && isFullyPaid(paidAmount, order.getTotalPrice())) {
            order.setStatus(CustomOrderStatus.READY_FOR_PICKUP);
        }
        order.setPaidAmount(paidAmount);
        customOrderRepository.save(order);
    }

    @Transactional
    public void handleCommonPaymentRefunded(UUID orderId, BigDecimal amount) {
        CommonCarOrder order = findCommonOrderOrThrow(orderId);
        order.setPaidAmount(subtractRefund(order.getPaidAmount(), amount));
        order.setStatus(CommonOrderStatus.REFUND_REQUIRED);
        commonOrderRepository.save(order);
    }

    @Transactional
    public void handleCustomPaymentRefunded(UUID orderId, BigDecimal amount) {
        CustomCarOrder order = findCustomOrderOrThrow(orderId);
        order.setPaidAmount(subtractRefund(order.getPaidAmount(), amount));
        order.setStatus(CustomOrderStatus.REFUND_REQUIRED);
        customOrderRepository.save(order);
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

    private Optional<CartCheckoutResponse> findExistingCheckout(UUID cartId) {
        Optional<CommonCarOrder> commonOrder = commonOrderRepository.findByCartId(cartId);
        if (commonOrder.isPresent()) {
            return Optional.of(CartCheckoutResponse.stockCar(
                    cartId, OrderMapper.INSTANCE.toCommonOrderResponse(commonOrder.get())));
        }
        return customOrderRepository.findByCartId(cartId)
                .map(order -> CartCheckoutResponse.configuredCar(
                        cartId, OrderMapper.INSTANCE.toCustomOrderResponse(order)));
    }

    private void validateSnapshotOwner(CartSnapshotResponse snapshot, UUID customerId) {
        if (!snapshot.customerId().equals(customerId)) {
            throw new DomainValidationException("Cart snapshot does not belong to current user");
        }
    }

    private void validateQuotedPrice(UUID orderId, InventoryReservationResponse reservation, BigDecimal quotedPrice) {
        if (quotedPrice == null) {
            return;
        }
        if (reservation.totalPrice() == null || quotedPrice.compareTo(reservation.totalPrice()) != 0) {
            inventoryReservationClient.releaseReservation(
                    orderId, reservation.reservationId(), "Cart quote changed before checkout");
            throw new DomainValidationException("Cart quote has changed; refresh cart before checkout");
        }
    }

    private void releaseCartAfterFailure(UUID cartId, UUID customerId, RuntimeException originalException) {
        try {
            cartSnapshotClient.releaseCart(cartId, customerId);
        } catch (RuntimeException releaseException) {
            originalException.addSuppressed(releaseException);
        }
    }

    private void validateReservationId(UUID reservationId) {
        if (reservationId == null) {
            throw new DomainValidationException("Order has no held reservation");
        }
    }

    private void confirmHeldReservation(UUID orderId, UUID reservationId) {
        validateReservationId(reservationId);
        inventoryReservationClient.confirmReservation(orderId, reservationId);
    }

    private BigDecimal addPayment(BigDecimal current, BigDecimal payment, BigDecimal total) {
        BigDecimal updated = (current == null ? BigDecimal.ZERO : current).add(payment);
        if (total == null || updated.compareTo(total) > 0) {
            throw new DomainValidationException("Payment exceeds outstanding order amount");
        }
        return updated;
    }

    private boolean isFullyPaid(BigDecimal paidAmount, BigDecimal totalPrice) {
        return paidAmount != null && totalPrice != null && paidAmount.compareTo(totalPrice) >= 0;
    }

    private BigDecimal subtractRefund(BigDecimal current, BigDecimal refund) {
        return (current == null ? BigDecimal.ZERO : current).subtract(refund).max(BigDecimal.ZERO);
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

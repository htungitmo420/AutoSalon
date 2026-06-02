package org.example.orderservice.application.service;

import org.example.orderservice.application.client.InventoryReservationClient;
import org.example.orderservice.application.client.CartSnapshotClient;
import org.example.orderservice.application.dto.request.CommonOrderRequest;
import org.example.orderservice.application.dto.request.CustomOrderRequest;
import org.example.orderservice.application.dto.response.CommonOrderResponse;
import org.example.orderservice.application.dto.response.CustomOrderResponse;
import org.example.orderservice.application.dto.response.InventoryReservationResponse;
import org.example.orderservice.application.port.out.CurrentUserProvider;
import org.example.orderservice.application.port.out.OrderWorkflowEventPublisher;
import org.example.orderservice.domain.exceptions.DomainValidationException;
import org.example.orderservice.domain.exceptions.EntityNotFoundException;
import org.example.orderservice.domain.order.enums.CommonOrderStatus;
import org.example.orderservice.domain.order.enums.CustomOrderStatus;
import org.example.orderservice.infrastructure.inmemory.InMemoryCommonOrderRepository;
import org.example.orderservice.infrastructure.inmemory.InMemoryCustomOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CAR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MODEL_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID WHEELS_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final BigDecimal CONFIGURED_PRICE = BigDecimal.valueOf(2300000);

    private OrderService orderService;
    private InventoryReservationClient inventoryReservationClient;

    @BeforeEach
    void setUp() {
        CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
        when(currentUserProvider.getCurrentUserId()).thenReturn(CUSTOMER_ID);
        when(currentUserProvider.hasElevatedReadAccess()).thenReturn(false);
        inventoryReservationClient = mock(InventoryReservationClient.class);
        when(inventoryReservationClient.reserveStockCar(any(), any(), any()))
                .thenReturn(reservation(BigDecimal.valueOf(2500000)));
        when(inventoryReservationClient.reserveConfiguration(any(), any(), any(), any()))
                .thenReturn(reservation(CONFIGURED_PRICE));
        when(inventoryReservationClient.confirmReservation(any(), any()))
                .thenReturn(reservation(CONFIGURED_PRICE));

        orderService = new OrderService(
                new InMemoryCommonOrderRepository(),
                new InMemoryCustomOrderRepository(),
                currentUserProvider,
                inventoryReservationClient,
                mock(CartSnapshotClient.class),
                mock(OrderWorkflowEventPublisher.class)
        );
    }

    private CommonOrderResponse placeCommonOrder() {
        return orderService.placeCommonOrder(new CommonOrderRequest(CAR_ID, UUID.randomUUID()));
    }

    private CustomOrderResponse placeCustomOrder(Map<String, UUID> selectedPartIds) {
        return orderService.placeCustomOrder(new CustomOrderRequest(MODEL_ID, UUID.randomUUID(), selectedPartIds));
    }

    @Test
    void placeCommonOrder_reservesStockBeforeWaitingForPayment() {
        CommonOrderResponse response = placeCommonOrder();

        assertEquals(CAR_ID, response.carId());
        assertEquals(CUSTOMER_ID, response.customerId());
        assertEquals(CommonOrderStatus.WAITING_FOR_PAYMENT, response.status());
    }

    @Test
    void getCommonOrder_returnsOrder() {
        CommonOrderResponse created = placeCommonOrder();

        assertEquals(created.id(), orderService.getCommonOrder(created.id()).id());
    }

    @Test
    void getCommonOrder_notFound() {
        assertThrows(EntityNotFoundException.class, () -> orderService.getCommonOrder(UUID.randomUUID()));
    }

    @Test
    void listCommonOrders_returnsCurrentUserOrders() {
        placeCommonOrder();
        placeCommonOrder();

        assertEquals(2, orderService.listCommonOrders().size());
    }

    @Test
    void deleteCommonOrder_removesOrder() {
        CommonOrderResponse order = placeCommonOrder();

        orderService.deleteCommonOrder(order.id());

        assertThrows(EntityNotFoundException.class, () -> orderService.getCommonOrder(order.id()));
    }

    @Test
    void deleteCommonOrder_notFound() {
        assertThrows(EntityNotFoundException.class, () -> orderService.deleteCommonOrder(UUID.randomUUID()));
    }

    @Test
    void placeCustomOrder_reservesConfigurationAndStoresQuotedPrice() {
        CustomOrderResponse response = placeCustomOrder(Map.of("WHEELS", WHEELS_ID));

        assertEquals(MODEL_ID, response.modelId());
        assertEquals(CUSTOMER_ID, response.customerId());
        assertEquals(Map.of("WHEELS", WHEELS_ID), response.selectedPartIds());
        assertEquals(CustomOrderStatus.WAITING_FOR_PAYMENT, response.status());
        assertEquals(CONFIGURED_PRICE, response.totalPrice());
    }

    @Test
    void placeCustomOrder_nullSelectedPartsBecomesEmptyMap() {
        CustomOrderResponse response = placeCustomOrder(null);

        assertEquals(Map.of(), response.selectedPartIds());
    }

    @Test
    void getCustomOrder_returnsOrder() {
        CustomOrderResponse created = placeCustomOrder(Map.of());

        assertEquals(created.id(), orderService.getCustomOrder(created.id()).id());
    }

    @Test
    void getCustomOrder_notFound() {
        assertThrows(EntityNotFoundException.class, () -> orderService.getCustomOrder(UUID.randomUUID()));
    }

    @Test
    void listCustomOrders_returnsCurrentUserOrders() {
        placeCustomOrder(Map.of());
        placeCustomOrder(Map.of());

        assertEquals(2, orderService.listCustomOrders().size());
    }

    @Test
    void deleteCustomOrder_removesOrder() {
        CustomOrderResponse order = placeCustomOrder(Map.of());

        orderService.deleteCustomOrder(order.id());

        assertThrows(EntityNotFoundException.class, () -> orderService.getCustomOrder(order.id()));
    }

    @Test
    void deleteCustomOrder_notFound() {
        assertThrows(EntityNotFoundException.class, () -> orderService.deleteCustomOrder(UUID.randomUUID()));
    }

    @Test
    void commonOrder_statusTransitions() {
        CommonOrderResponse order = placeCommonOrder();
        assertEquals(CommonOrderStatus.WAITING_FOR_PAYMENT, order.status());

        order = orderService.markCommonOrderPaid(order.id());
        assertEquals(CommonOrderStatus.PAID, order.status());

        order = orderService.markCommonOrderReadyForPickup(order.id());
        assertEquals(CommonOrderStatus.READY_FOR_PICKUP, order.status());

        order = orderService.completeCommonOrder(order.id());
        assertEquals(CommonOrderStatus.COMPLETED, order.status());
    }

    @Test
    void commonOrder_cancel() {
        CommonOrderResponse order = orderService.cancelCommonOrder(placeCommonOrder().id());

        assertEquals(CommonOrderStatus.CANCELLED, order.status());
    }

    @Test
    void commonOrder_invalidTransition() {
        CommonOrderResponse order = placeCommonOrder();

        assertThrows(DomainValidationException.class, () -> orderService.approveCommonOrder(order.id()));
    }

    @Test
    void customOrder_statusTransitions() {
        CustomOrderResponse order = placeCustomOrder(Map.of());
        assertEquals(CustomOrderStatus.WAITING_FOR_PAYMENT, order.status());

        order = orderService.markCustomOrderPaid(order.id());
        assertEquals(CustomOrderStatus.ASSEMBLING, order.status());

        orderService.handleAssemblyCompleted(order.id());
        order = orderService.getCustomOrder(order.id());
        assertEquals(CustomOrderStatus.READY_FOR_PICKUP, order.status());

        order = orderService.completeCustomOrder(order.id());
        assertEquals(CustomOrderStatus.COMPLETED, order.status());
    }

    @Test
    void customOrder_cancel() {
        CustomOrderResponse order = orderService.cancelCustomOrder(placeCustomOrder(Map.of()).id());

        assertEquals(CustomOrderStatus.CANCELLED, order.status());
    }

    private InventoryReservationResponse reservation(BigDecimal price) {
        return new InventoryReservationResponse(
                UUID.randomUUID(), "HELD", Instant.now().plusSeconds(900), price);
    }
}

package org.example.orderservice.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.dto.response.CommonOrderResponse;
import org.example.orderservice.application.dto.response.CustomOrderResponse;
import org.example.orderservice.application.dto.response.PageResponse;
import org.example.orderservice.application.service.AdminOrderQueryService;
import org.example.orderservice.application.service.OrderService;
import org.example.orderservice.domain.order.enums.CommonOrderStatus;
import org.example.orderservice.domain.order.enums.CustomOrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/orders")
@Tag(name = "Admin Orders", description = "Order administration and status transitions")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class AdminOrderController {

    private final AdminOrderQueryService queryService;
    private final OrderService orderService;

    @GetMapping("/common")
    @Operation(summary = "List stock-car orders with paging and filters")
    public PageResponse<CommonOrderResponse> listCommon(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) CommonOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return queryService.listCommonOrders(customerId, status, page, size, sortBy, sortDirection);
    }

    @GetMapping("/common/{orderId}")
    public CommonOrderResponse getCommon(@PathVariable UUID orderId) {
        return orderService.getCommonOrder(orderId);
    }

    @PatchMapping("/common/{orderId}/mark-paid")
    public CommonOrderResponse markCommonPaid(@PathVariable UUID orderId) {
        return orderService.markCommonOrderPaid(orderId);
    }

    @PatchMapping("/common/{orderId}/ready-for-pickup")
    public CommonOrderResponse markCommonReadyForPickup(@PathVariable UUID orderId) {
        return orderService.markCommonOrderReadyForPickup(orderId);
    }

    @PatchMapping("/common/{orderId}/complete")
    public CommonOrderResponse completeCommon(@PathVariable UUID orderId) {
        return orderService.completeCommonOrder(orderId);
    }

    @PatchMapping("/common/{orderId}/cancel")
    public CommonOrderResponse cancelCommon(@PathVariable UUID orderId) {
        return orderService.cancelCommonOrder(orderId);
    }

    @DeleteMapping("/common/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommon(@PathVariable UUID orderId) {
        orderService.deleteCommonOrder(orderId);
    }

    @GetMapping("/custom")
    @Operation(summary = "List configured-car orders with paging and filters")
    public PageResponse<CustomOrderResponse> listCustom(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) CustomOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return queryService.listCustomOrders(customerId, status, page, size, sortBy, sortDirection);
    }

    @GetMapping("/custom/{orderId}")
    public CustomOrderResponse getCustom(@PathVariable UUID orderId) {
        return orderService.getCustomOrder(orderId);
    }

    @PatchMapping("/custom/{orderId}/mark-paid")
    public CustomOrderResponse markCustomPaid(@PathVariable UUID orderId) {
        return orderService.markCustomOrderPaid(orderId);
    }

    @PatchMapping("/custom/{orderId}/waiting-for-delivery")
    public CustomOrderResponse markCustomWaitingForDelivery(@PathVariable UUID orderId) {
        return orderService.markCustomOrderWaitingForDelivery(orderId);
    }

    @PatchMapping("/custom/{orderId}/ready-for-pickup")
    public CustomOrderResponse markCustomReadyForPickup(@PathVariable UUID orderId) {
        return orderService.markCustomOrderReadyForPickup(orderId);
    }

    @PatchMapping("/custom/{orderId}/complete")
    public CustomOrderResponse completeCustom(@PathVariable UUID orderId) {
        return orderService.completeCustomOrder(orderId);
    }

    @PatchMapping("/custom/{orderId}/cancel")
    public CustomOrderResponse cancelCustom(@PathVariable UUID orderId) {
        return orderService.cancelCustomOrder(orderId);
    }

    @DeleteMapping("/custom/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustom(@PathVariable UUID orderId) {
        orderService.deleteCustomOrder(orderId);
    }
}

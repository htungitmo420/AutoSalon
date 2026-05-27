package org.example.orderservice.presentation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.dto.request.CommonOrderRequest;
import org.example.orderservice.application.dto.request.CustomOrderRequest;
import org.example.orderservice.application.dto.response.CommonOrderResponse;
import org.example.orderservice.application.dto.response.CustomOrderResponse;
import org.example.orderservice.application.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Tag(name = "Orders")
public class OrderController {

	private final OrderService orderService;

	@PostMapping("/common")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public CommonOrderResponse placeCommon(@RequestBody CommonOrderRequest request) {
		return orderService.placeCommonOrder(request);
	}

	@GetMapping("/common/{orderId}")
	@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
	public CommonOrderResponse getCommon(@PathVariable UUID orderId) {
		return orderService.getCommonOrder(orderId);
	}

	@GetMapping("/common")
	@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
	public List<CommonOrderResponse> listCommon() {
		return orderService.listCommonOrders();
	}

	@DeleteMapping("/common/{orderId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAnyRole('ADMIN')")
	public void deleteCommon(@PathVariable UUID orderId) {
		orderService.deleteCommonOrder(orderId);
	}

	@PatchMapping("/common/{orderId}/approve")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public CommonOrderResponse approveCommon(@PathVariable UUID orderId) {
		return orderService.approveCommonOrder(orderId);
	}

	@PatchMapping("/common/{orderId}/request-payment")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public CommonOrderResponse requestCommonPayment(@PathVariable UUID orderId) {
		return orderService.requestCommonOrderPayment(orderId);
	}

	@PatchMapping("/common/{orderId}/mark-paid")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public CommonOrderResponse markCommonPaid(@PathVariable UUID orderId) {
		return orderService.markCommonOrderPaid(orderId);
	}

	@PatchMapping("/common/{orderId}/ready-for-pickup")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public CommonOrderResponse markCommonReadyForPickup(@PathVariable UUID orderId) {
		return orderService.markCommonOrderReadyForPickup(orderId);
	}

	@PatchMapping("/common/{orderId}/complete")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public CommonOrderResponse completeCommon(@PathVariable UUID orderId) {
		return orderService.completeCommonOrder(orderId);
	}

	@PatchMapping("/common/{orderId}/cancel")
	@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
	public CommonOrderResponse cancelCommon(@PathVariable UUID orderId) {
		return orderService.cancelCommonOrder(orderId);
	}

	@PostMapping("/custom")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public CustomOrderResponse placeCustom(@RequestBody CustomOrderRequest request) {
		return orderService.placeCustomOrder(request);
	}

	@GetMapping("/custom/{orderId}")
	@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
	public CustomOrderResponse getCustom(@PathVariable UUID orderId) {
		return orderService.getCustomOrder(orderId);
	}

	@GetMapping("/custom")
	@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
	public List<CustomOrderResponse> listCustom() {
		return orderService.listCustomOrders();
	}

	@DeleteMapping("/custom/{orderId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAnyRole('ADMIN')")
	public void deleteCustom(@PathVariable UUID orderId) {
		orderService.deleteCustomOrder(orderId);
	}

	@PatchMapping("/custom/{orderId}/approve")
	@PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
	public CustomOrderResponse approveCustom(@PathVariable UUID orderId) {
		return orderService.approveCustomOrder(orderId);
	}

	@PatchMapping("/custom/{orderId}/request-payment")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public CustomOrderResponse requestCustomPayment(@PathVariable UUID orderId) {
		return orderService.requestCustomOrderPayment(orderId);
	}

	@PatchMapping("/custom/{orderId}/mark-paid")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public CustomOrderResponse markCustomPaid(@PathVariable UUID orderId) {
		return orderService.markCustomOrderPaid(orderId);
	}

	@PatchMapping("/custom/{orderId}/waiting-for-delivery")
	@PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
	public CustomOrderResponse markCustomWaitingForDelivery(@PathVariable UUID orderId) {
		return orderService.markCustomOrderWaitingForDelivery(orderId);
	}

	@PatchMapping("/custom/{orderId}/ready-for-pickup")
	@PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
	public CustomOrderResponse markCustomReadyForPickup(@PathVariable UUID orderId) {
		return orderService.markCustomOrderReadyForPickup(orderId);
	}

	@PatchMapping("/custom/{orderId}/complete")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public CustomOrderResponse completeCustom(@PathVariable UUID orderId) {
		return orderService.completeCustomOrder(orderId);
	}

	@PatchMapping("/custom/{orderId}/cancel")
	@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
	public CustomOrderResponse cancelCustom(@PathVariable UUID orderId) {
		return orderService.cancelCustomOrder(orderId);
	}
}

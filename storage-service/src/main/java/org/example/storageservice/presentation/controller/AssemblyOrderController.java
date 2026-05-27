package org.example.storageservice.presentation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.SaveAssemblyOrderRequest;
import org.example.storageservice.application.dto.response.AssemblyOrderResponse;
import org.example.storageservice.application.service.AssemblyOrderService;
import org.example.storageservice.application.service.InventoryReservationService;
import org.example.storageservice.infrastructure.logging.TraceContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assembly-orders")
@Tag(name = "Assembly Orders")
@PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
public class AssemblyOrderController {

    private final AssemblyOrderService assemblyOrderService;
    private final InventoryReservationService inventoryReservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssemblyOrderResponse create(@RequestBody SaveAssemblyOrderRequest request) {
        return assemblyOrderService.createAssemblyOrder(request);
    }

    @GetMapping("/{assemblyOrderId}")
    public AssemblyOrderResponse getById(@PathVariable UUID assemblyOrderId) {
        return assemblyOrderService.getAssemblyOrder(assemblyOrderId);
    }

    @GetMapping
    public List<AssemblyOrderResponse> list() {
        return assemblyOrderService.listAssemblyOrders();
    }

    @PutMapping("/{assemblyOrderId}")
    public AssemblyOrderResponse update(@PathVariable UUID assemblyOrderId,
                                        @RequestBody SaveAssemblyOrderRequest request) {
        return assemblyOrderService.updateAssemblyOrder(assemblyOrderId, request);
    }

    @PatchMapping("/{assemblyOrderId}/assemble")
    public AssemblyOrderResponse assemble(@PathVariable UUID assemblyOrderId) {
        return inventoryReservationService.assemble(assemblyOrderId, TraceContext.currentTraceId());
    }

    @PatchMapping("/{assemblyOrderId}/fail")
    public AssemblyOrderResponse fail(@PathVariable UUID assemblyOrderId,
                                      @RequestParam(required = false) String reason) {
        return inventoryReservationService.failAssembly(assemblyOrderId, reason, TraceContext.currentTraceId());
    }

    @DeleteMapping("/{assemblyOrderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID assemblyOrderId) {
        assemblyOrderService.deleteAssemblyOrder(assemblyOrderId);
    }
}

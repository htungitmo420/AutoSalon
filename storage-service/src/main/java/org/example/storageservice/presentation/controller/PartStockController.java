package org.example.storageservice.presentation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.SavePartStockRequest;
import org.example.storageservice.application.dto.response.PartStockResponse;
import org.example.storageservice.application.service.PartStockService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/part-stocks")
@Tag(name = "Part Stocks")
@PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
public class PartStockController {

    private final PartStockService partStockService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartStockResponse create(@RequestBody SavePartStockRequest request) {
        return partStockService.createPartStock(request);
    }

    @GetMapping("/{stockId}")
    public PartStockResponse getById(@PathVariable UUID stockId) {
        return partStockService.getPartStock(stockId);
    }

    @GetMapping
    public List<PartStockResponse> list() {
        return partStockService.listPartStocks();
    }

    @PutMapping("/{stockId}")
    public PartStockResponse update(@PathVariable UUID stockId, @RequestBody SavePartStockRequest request) {
        return partStockService.updatePartStock(stockId, request);
    }

    @DeleteMapping("/{stockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID stockId) {
        partStockService.deletePartStock(stockId);
    }
}
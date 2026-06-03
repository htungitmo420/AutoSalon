package org.example.storageservice.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.CreateCatalogAssetUploadRequest;
import org.example.storageservice.application.dto.response.CatalogAssetResponse;
import org.example.storageservice.application.dto.response.CatalogAssetUploadResponse;
import org.example.storageservice.application.service.CatalogAssetService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1")
public class CatalogAssetController {
    private final CatalogAssetService assetService;

    @PostMapping("/admin/catalog-assets/upload-url")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogAssetUploadResponse createUpload(@Valid @RequestBody CreateCatalogAssetUploadRequest request) {
        return assetService.createUpload(request);
    }

    @PostMapping("/admin/catalog-assets/{assetId}/complete")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public CatalogAssetResponse complete(@PathVariable UUID assetId) {
        return assetService.complete(assetId);
    }

    @DeleteMapping("/admin/catalog-assets/{assetId}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID assetId) {
        assetService.delete(assetId);
    }

    @GetMapping("/catalog/models/{modelId}/assets")
    public List<CatalogAssetResponse> listActive(@PathVariable UUID modelId) {
        return assetService.listActive(modelId);
    }
}

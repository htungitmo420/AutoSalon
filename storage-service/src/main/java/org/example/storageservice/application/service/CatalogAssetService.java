package org.example.storageservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.dto.request.CreateCatalogAssetUploadRequest;
import org.example.storageservice.application.dto.response.CatalogAssetResponse;
import org.example.storageservice.application.dto.response.CatalogAssetUploadResponse;
import org.example.storageservice.application.port.out.CatalogAssetStorage;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.CatalogAssetRepository;
import org.example.storageservice.domain.asset.AssetStatus;
import org.example.storageservice.domain.asset.CatalogAsset;
import org.example.storageservice.domain.exceptions.DomainValidationException;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogAssetService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final CatalogAssetRepository assetRepository;
    private final CarModelRepository carModelRepository;
    private final CatalogAssetStorage assetStorage;

    @Value("${storage.assets.max-size-bytes:10485760}")
    private long maxSizeBytes;

    @Value("${storage.assets.presigned-url-ttl-seconds:900}")
    private int presignedUrlTtlSeconds;

    @Transactional
    public CatalogAssetUploadResponse createUpload(CreateCatalogAssetUploadRequest request) {
        validate(request);
        if (carModelRepository.findById(request.modelId()).isEmpty()) {
            throw new EntityNotFoundException("Car model not found: " + request.modelId());
        }
        UUID assetId = UUID.randomUUID();
        Instant now = Instant.now();
        CatalogAsset asset = assetRepository.save(CatalogAsset.builder()
                .id(assetId)
                .modelId(request.modelId())
                .objectKey("catalog/models/" + request.modelId() + "/" + assetId + "-" + sanitize(request.fileName()))
                .fileName(request.fileName().trim())
                .contentType(request.contentType().toLowerCase(Locale.ROOT))
                .status(AssetStatus.PENDING_UPLOAD)
                .createdAt(now)
                .updatedAt(now)
                .build());
        return new CatalogAssetUploadResponse(
                toResponse(asset, null, null),
                assetStorage.createUploadUrl(asset.getObjectKey(), presignedUrlTtlSeconds),
                now.plusSeconds(presignedUrlTtlSeconds));
    }

    @Transactional
    public CatalogAssetResponse complete(UUID assetId) {
        CatalogAsset asset = requireAsset(assetId);
        CatalogAssetStorage.StoredObject uploaded = assetStorage.stat(asset.getObjectKey());
        if (uploaded.sizeBytes() <= 0 || uploaded.sizeBytes() > maxSizeBytes) {
            assetStorage.delete(asset.getObjectKey());
            throw new DomainValidationException("Uploaded image must be between 1 and " + maxSizeBytes + " bytes");
        }
        if (!asset.getContentType().equalsIgnoreCase(uploaded.contentType())) {
            assetStorage.delete(asset.getObjectKey());
            throw new DomainValidationException("Uploaded image content type does not match requested content type");
        }
        asset.activate(uploaded.sizeBytes(), Instant.now());
        return withDownloadUrl(assetRepository.save(asset));
    }

    @Transactional(readOnly = true)
    public List<CatalogAssetResponse> listActive(UUID modelId) {
        return assetRepository.findActiveByModelId(modelId).stream()
                .map(this::withDownloadUrl)
                .toList();
    }

    @Transactional
    public void delete(UUID assetId) {
        CatalogAsset asset = requireAsset(assetId);
        assetStorage.delete(asset.getObjectKey());
        asset.remove(Instant.now());
        assetRepository.save(asset);
    }

    private CatalogAsset requireAsset(UUID assetId) {
        return assetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("Catalog asset not found: " + assetId));
    }

    private CatalogAssetResponse withDownloadUrl(CatalogAsset asset) {
        Instant expiresAt = Instant.now().plusSeconds(presignedUrlTtlSeconds);
        return toResponse(asset, assetStorage.createDownloadUrl(asset.getObjectKey(), presignedUrlTtlSeconds), expiresAt);
    }

    private CatalogAssetResponse toResponse(CatalogAsset asset, String downloadUrl, Instant expiresAt) {
        return new CatalogAssetResponse(asset.getId(), asset.getModelId(), asset.getFileName(), asset.getContentType(),
                asset.getSizeBytes(), asset.getStatus(), downloadUrl, expiresAt);
    }

    private void validate(CreateCatalogAssetUploadRequest request) {
        if (request == null || request.modelId() == null || request.fileName() == null || request.fileName().isBlank()
                || request.contentType() == null || !ALLOWED_TYPES.contains(request.contentType().toLowerCase(Locale.ROOT))) {
            throw new DomainValidationException("modelId, fileName and supported image contentType are required");
        }
    }

    private String sanitize(String fileName) {
        String sanitized = fileName.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "-");
        return sanitized.isBlank() ? "image" : sanitized;
    }
}

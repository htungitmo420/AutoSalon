package org.example.storageservice.application.dto.response;

import org.example.storageservice.domain.asset.AssetStatus;

import java.time.Instant;
import java.util.UUID;

public record CatalogAssetResponse(
        UUID id,
        UUID modelId,
        String fileName,
        String contentType,
        Long sizeBytes,
        AssetStatus status,
        String downloadUrl,
        Instant downloadUrlExpiresAt
) {
}

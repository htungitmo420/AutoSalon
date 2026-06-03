package org.example.storageservice.application.dto.response;

import java.time.Instant;

public record CatalogAssetUploadResponse(
        CatalogAssetResponse asset,
        String uploadUrl,
        Instant uploadUrlExpiresAt
) {
}

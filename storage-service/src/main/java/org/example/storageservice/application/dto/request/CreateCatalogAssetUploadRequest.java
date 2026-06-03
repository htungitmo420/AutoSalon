package org.example.storageservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCatalogAssetUploadRequest(
        @NotNull
        UUID modelId,
        @NotBlank
        String fileName,
        @NotBlank
        String contentType
) {
}

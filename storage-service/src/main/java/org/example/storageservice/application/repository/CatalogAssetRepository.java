package org.example.storageservice.application.repository;

import org.example.storageservice.domain.asset.CatalogAsset;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogAssetRepository {
    CatalogAsset save(CatalogAsset asset);
    Optional<CatalogAsset> findById(UUID assetId);
    List<CatalogAsset> findActiveByModelId(UUID modelId);
}

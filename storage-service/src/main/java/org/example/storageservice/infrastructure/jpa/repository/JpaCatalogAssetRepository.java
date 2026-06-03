package org.example.storageservice.infrastructure.jpa.repository;

import org.example.storageservice.domain.asset.AssetStatus;
import org.example.storageservice.domain.asset.CatalogAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaCatalogAssetRepository extends JpaRepository<CatalogAsset, UUID> {
    List<CatalogAsset> findAllByModelIdAndStatusOrderByCreatedAtAsc(UUID modelId, AssetStatus status);
}

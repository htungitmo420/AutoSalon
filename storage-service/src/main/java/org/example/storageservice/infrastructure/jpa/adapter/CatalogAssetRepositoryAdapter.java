package org.example.storageservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.repository.CatalogAssetRepository;
import org.example.storageservice.domain.asset.AssetStatus;
import org.example.storageservice.domain.asset.CatalogAsset;
import org.example.storageservice.infrastructure.jpa.repository.JpaCatalogAssetRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CatalogAssetRepositoryAdapter implements CatalogAssetRepository {
    private final JpaCatalogAssetRepository repository;

    @Override
    public CatalogAsset save(CatalogAsset asset) {
        return repository.save(asset);
    }

    @Override
    public Optional<CatalogAsset> findById(UUID assetId) {
        return repository.findById(assetId);
    }

    @Override
    public List<CatalogAsset> findActiveByModelId(UUID modelId) {
        return repository.findAllByModelIdAndStatusOrderByCreatedAtAsc(modelId, AssetStatus.ACTIVE);
    }
}

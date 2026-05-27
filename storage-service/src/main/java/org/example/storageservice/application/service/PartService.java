package org.example.storageservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.storageservice.application.mapper.PartMapper;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.application.repository.CarModelRepository;
import org.example.storageservice.application.repository.PartRepository;
import org.example.storageservice.domain.exceptions.EntityNotFoundException;
import org.example.storageservice.domain.part.models.Part;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final CarModelRepository carModelRepository;

    @Transactional
    public PartResponse createPart(SavePartRequest request) {
        validateCompatibleModels(request.compatibleModelIds());

        Part part = PartMapper.INSTANCE.toPart(request);

        return PartMapper.INSTANCE.toPartResponse(partRepository.save(part));
    }

    @Transactional(readOnly = true)
    public PartResponse getPart(UUID partId) {
        return PartMapper.INSTANCE.toPartResponse(
                partRepository.findById(partId)
                        .orElseThrow(() -> new EntityNotFoundException("Part not found: " + partId))
        );
    }

    @Transactional(readOnly = true)
    public List<PartResponse> listParts() {
        return partRepository.findAll().stream()
                .map(PartMapper.INSTANCE::toPartResponse)
                .toList();
    }

    @Transactional
    public PartResponse updatePart(UUID partId, SavePartRequest request) {
        Part existing = partRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException("Part not found: " + partId));

        validateCompatibleModels(request.compatibleModelIds());

        Part updated = PartMapper.INSTANCE.toPart(request, existing.getId());
        return PartMapper.INSTANCE.toPartResponse(partRepository.save(updated));
    }

    @Transactional
    public void deletePart(UUID partId) {
        if (!partRepository.deleteById(partId)) {
            throw new EntityNotFoundException("Part not found: " + partId);
        }
    }

    private void validateCompatibleModels(Set<UUID> modelIds) {
        if (modelIds == null) return;
        modelIds.forEach(modelId ->
                carModelRepository.findById(modelId)
                        .orElseThrow(() -> new EntityNotFoundException("Car model not found: " + modelId))
        );
    }
}
package org.example.storageservice.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Builder;
import org.mapstruct.factory.Mappers;
import org.example.storageservice.application.dto.request.SavePartRequest;
import org.example.storageservice.application.dto.response.PartResponse;
import org.example.storageservice.domain.part.models.Part;

import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface PartMapper {

    PartMapper INSTANCE = Mappers.getMapper(PartMapper.class);

    PartResponse toPartResponse(Part part);

    Part toPart(SavePartRequest request);

    @Mapping(target = "id", source = "existingId")
    Part toPart(SavePartRequest request, UUID existingId);
}
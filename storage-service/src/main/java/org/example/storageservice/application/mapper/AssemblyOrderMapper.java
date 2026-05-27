package org.example.storageservice.application.mapper;

import org.example.storageservice.application.dto.request.SaveAssemblyOrderRequest;
import org.example.storageservice.application.dto.response.AssemblyOrderResponse;
import org.example.storageservice.domain.assembly.enums.AssemblyOrderStatus;
import org.example.storageservice.domain.assembly.model.AssemblyOrder;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface AssemblyOrderMapper {

    AssemblyOrderMapper INSTANCE = Mappers.getMapper(AssemblyOrderMapper.class);

    @Mapping(target = "requiredPartIds", source = "requiredPartIds", qualifiedByName = "readOnlyRequiredPartIds")
    AssemblyOrderResponse toAssemblyOrderResponse(AssemblyOrder order);

    @Mapping(target = "status", source = "status", qualifiedByName = "statusOrCreated")
    @Mapping(target = "requiredPartIds", source = "requiredPartIds", qualifiedByName = "entityRequiredPartIds")
    AssemblyOrder toAssemblyOrder(SaveAssemblyOrderRequest request);

    @Mapping(target = "status", source = "status", qualifiedByName = "statusOrCreated")
    @Mapping(target = "requiredPartIds", source = "requiredPartIds", qualifiedByName = "entityRequiredPartIds")
    void updateAssemblyOrder(SaveAssemblyOrderRequest request, @MappingTarget AssemblyOrder order);

    @Named("statusOrCreated")
    default AssemblyOrderStatus statusOrCreated(AssemblyOrderStatus status) {
        return status == null ? AssemblyOrderStatus.CREATED : status;
    }

    @Named("readOnlyRequiredPartIds")
    default Map<String, UUID> copyRequiredPartIds(Map<String, UUID> requiredPartIds) {
        return requiredPartIds == null ? Map.of() : Map.copyOf(requiredPartIds);
    }

    @Named("entityRequiredPartIds")
    default Map<String, UUID> toEntityRequiredPartIds(Map<String, UUID> requiredPartIds) {
        return requiredPartIds == null ? new HashMap<>() : new HashMap<>(requiredPartIds);
    }
}

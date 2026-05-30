package org.example.storageservice.application.mapper;

import org.example.storageservice.application.dto.request.SaveAssemblyOrderRequest;
import org.example.storageservice.application.dto.response.CarConfigurationResponse;
import org.example.storageservice.application.dto.response.InventoryReservationResponse;
import org.example.storageservice.domain.reservation.model.InventoryReservation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface InventoryReservationMapper {

    InventoryReservationMapper INSTANCE = Mappers.getMapper(InventoryReservationMapper.class);

    @Mapping(target = "requiredPartIds", source = "requiredPartIds", qualifiedByName = "readOnlyRequiredPartIds")
    InventoryReservationResponse toInventoryReservationResponse(InventoryReservation reservation);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "sourceOrderType", constant = "COMMON")
    @Mapping(target = "carId", source = "carId")
    @Mapping(target = "requiredPartIds", expression = "java(new java.util.HashMap<>())")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "status", constant = "HELD")
    @Mapping(target = "expiresAt", source = "expiresAt")
    InventoryReservation toStockReservation(UUID orderId, UUID carId, BigDecimal totalPrice, Instant expiresAt);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "sourceOrderType", constant = "CUSTOM")
    @Mapping(target = "modelId", source = "modelId")
    @Mapping(target = "requiredPartIds", source = "requiredPartIds", qualifiedByName = "mutableRequiredPartIds")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "status", constant = "HELD")
    @Mapping(target = "expiresAt", source = "expiresAt")
    InventoryReservation toConfigurationReservation(UUID orderId, UUID modelId, Map<String, UUID> requiredPartIds,
                                                     BigDecimal totalPrice, Instant expiresAt);

    @Mapping(target = "sourceOrderId", source = "orderId")
    @Mapping(target = "requiredPartIds", source = "requiredPartIds", qualifiedByName = "mutableRequiredPartIds")
    @Mapping(target = "warehouseEmployeeId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    SaveAssemblyOrderRequest toAssemblyOrderRequest(InventoryReservation reservation);

    default Map<String, UUID> toRequiredPartIds(CarConfigurationResponse configuration) {
        Map<String, UUID> requiredPartIds = new HashMap<>();
        configuration.selectedParts()
                .forEach((type, part) -> requiredPartIds.put(type.name(), part.id()));
        return requiredPartIds;
    }

    @Named("mutableRequiredPartIds")
    default Map<String, UUID> toMutableRequiredPartIds(Map<String, UUID> requiredPartIds) {
        return requiredPartIds == null ? new HashMap<>() : new HashMap<>(requiredPartIds);
    }

    @Named("readOnlyRequiredPartIds")
    default Map<String, UUID> toReadOnlyRequiredPartIds(Map<String, UUID> requiredPartIds) {
        return requiredPartIds == null ? Map.of() : Map.copyOf(requiredPartIds);
    }
}

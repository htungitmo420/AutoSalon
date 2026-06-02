package org.example.orderservice.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Builder;
import org.mapstruct.factory.Mappers;
import org.example.orderservice.application.dto.request.CommonOrderRequest;
import org.example.orderservice.application.dto.request.CustomOrderRequest;
import org.example.orderservice.application.dto.response.CommonOrderResponse;
import org.example.orderservice.application.dto.response.CustomOrderResponse;
import org.example.orderservice.domain.order.model.CommonCarOrder;
import org.example.orderservice.domain.order.model.CustomCarOrder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    CommonOrderResponse toCommonOrderResponse(CommonCarOrder order);

    CustomOrderResponse toCustomOrderResponse(CustomCarOrder order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "status", expression = "java(CommonOrderStatus.PENDING_RESERVATION)")
    @Mapping(target = "paidAmount", ignore = true)
    CommonCarOrder toCommonCarOrder(CommonOrderRequest request, UUID customerId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "modelId", source = "modelId")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "selectedPartIds", source = "finalConfig")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "status", expression = "java(CustomOrderStatus.PENDING_RESERVATION)")
    @Mapping(target = "paidAmount", ignore = true)
    CustomCarOrder toCustomCarOrder(CustomOrderRequest request, UUID modelId, UUID customerId,
                                    Map<String, UUID> finalConfig, BigDecimal totalPrice);
}

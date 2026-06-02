package org.example.cartservice.application.mapper;

import org.example.cartservice.application.dto.response.CartResponse;
import org.example.cartservice.domain.cart.model.Cart;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Map;
import java.util.UUID;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface CartMapper {

    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    @Mapping(target = "selectedPartIds", source = "selectedPartIds", qualifiedByName = "toImmutableMap")
    CartResponse toResponse(Cart cart);

    @Named("toImmutableMap")
    default Map<String, UUID> toImmutableMap(Map<String, UUID> selectedPartIds) {
        return selectedPartIds == null ? Map.of() : Map.copyOf(selectedPartIds);
    }
}

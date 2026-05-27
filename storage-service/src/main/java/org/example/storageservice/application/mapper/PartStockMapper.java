package org.example.storageservice.application.mapper;

import org.example.storageservice.application.dto.request.SavePartStockRequest;
import org.example.storageservice.application.dto.response.PartStockResponse;
import org.example.storageservice.domain.stock.model.PartStock;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface PartStockMapper {

    PartStockMapper INSTANCE = Mappers.getMapper(PartStockMapper.class);

    PartStockResponse toPartStockResponse(PartStock stock);

    PartStock toPartStock(SavePartStockRequest request);

    void updatePartStock(SavePartStockRequest request, @MappingTarget PartStock stock);
}

package org.example.storageservice.application.mapper;

import org.example.storageservice.application.dto.response.AvailableCarResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Builder;
import org.mapstruct.factory.Mappers;
import org.example.storageservice.application.dto.request.SaveCarModelRequest;
import org.example.storageservice.application.dto.request.SaveCarRequest;
import org.example.storageservice.application.dto.response.CarConfigurationResponse;
import org.example.storageservice.application.dto.response.CarModelResponse;
import org.example.storageservice.application.dto.response.CarResponse;
import org.example.storageservice.domain.car.model.Car;
import org.example.storageservice.domain.car.model.CarModel;
import org.example.storageservice.domain.part.enums.PartType;
import org.example.storageservice.domain.part.models.Part;

import java.math.BigDecimal;
import java.util.Map;

@Mapper(uses = PartMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface CarMapper {

    CarMapper INSTANCE = Mappers.getMapper(CarMapper.class);

    CarResponse toCarResponse(Car car);

    Car toCar(SaveCarRequest request);

    CarModelResponse toCarModelResponse(CarModel model);

    CarModel toCarModel(SaveCarModelRequest request);

    @Mapping(target = "carModel", source = "model")
    @Mapping(target = "selectedParts", source = "selectedParts")
    @Mapping(target = "totalPrice", source = "totalPrice")
    CarConfigurationResponse toCarConfigurationResponse(CarModel model,
                                                        Map<PartType, Part> selectedParts,
                                                        BigDecimal totalPrice);

    @Mapping(target = "modelName", source = "model.modelName")
    @Mapping(target = "brand", source = "model.brand")
    @Mapping(target = "bodyType", source = "model.bodyType")
    @Mapping(target = "fuelType", source = "model.fuelType")
    AvailableCarResponse toAvailableCarResponse(Car car);
}
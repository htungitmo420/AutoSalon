package org.example.orderservice.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Builder;
import org.mapstruct.factory.Mappers;
import org.example.orderservice.application.dto.request.BookTestDriveRequest;
import org.example.orderservice.application.dto.response.TestDriveResponse;
import org.example.orderservice.domain.testdrive.model.TestDrive;

import java.util.UUID;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface TestDriveMapper {

    TestDriveMapper INSTANCE = Mappers.getMapper(TestDriveMapper.class);

    TestDriveResponse toTestDriveResponse(TestDrive testDrive);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "status", expression = "java(TestDriveStatus.PENDING)")
    TestDrive toTestDrive(BookTestDriveRequest request, UUID customerId);
}
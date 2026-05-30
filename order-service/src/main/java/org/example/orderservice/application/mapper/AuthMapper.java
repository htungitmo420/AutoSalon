package org.example.orderservice.application.mapper;

import org.example.orderservice.domain.auth.enums.AuthRole;
import org.example.orderservice.domain.auth.model.AuthUser;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface AuthMapper {

    AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "roles", expression = "java(defaultUserRoles())")
    AuthUser toRegisteredUser(String email, String passwordHash, String fullName);

    default Set<AuthRole> defaultUserRoles() {
        return Set.of(AuthRole.USER);
    }
}

package org.example.orderservice.application.repository;

import org.example.orderservice.domain.auth.model.AuthUser;

import java.util.Optional;

public interface AuthUserRepository extends Repository<AuthUser> {

    Optional<AuthUser> findByEmail(String email);
}

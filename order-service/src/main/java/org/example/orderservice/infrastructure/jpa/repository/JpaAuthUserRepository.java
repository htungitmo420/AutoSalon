package org.example.orderservice.infrastructure.jpa.repository;

import org.example.orderservice.domain.auth.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaAuthUserRepository extends JpaRepository<AuthUser, UUID> {

    Optional<AuthUser> findByIdAndRemovedFalse(UUID id);

    Optional<AuthUser> findByEmailAndRemovedFalse(String email);

    List<AuthUser> findAllByRemovedFalse();
}

package org.example.orderservice.infrastructure.jpa.adapter;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.application.repository.AuthUserRepository;
import org.example.orderservice.domain.auth.model.AuthUser;
import org.example.orderservice.infrastructure.jpa.repository.JpaAuthUserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AuthUserRepositoryAdapter implements AuthUserRepository {

    private final JpaAuthUserRepository delegate;

    @Override
    public AuthUser save(AuthUser entity) {
        return delegate.save(entity);
    }

    @Override
    public Optional<AuthUser> findById(UUID id) {
        return delegate.findByIdAndRemovedFalse(id);
    }

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        return delegate.findByEmailAndRemovedFalse(email);
    }

    @Override
    public List<AuthUser> findAll() {
        return delegate.findAllByRemovedFalse();
    }

    @Override
    public boolean deleteById(UUID id) {
        Optional<AuthUser> existing = delegate.findByIdAndRemovedFalse(id);
        if (existing.isEmpty()) {
            return false;
        }
        AuthUser user = existing.get();
        user.setRemoved(true);
        delegate.save(user);
        return true;
    }
}

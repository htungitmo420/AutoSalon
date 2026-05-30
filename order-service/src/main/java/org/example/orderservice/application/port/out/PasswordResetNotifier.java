package org.example.orderservice.application.port.out;

import org.example.orderservice.domain.auth.model.AuthUser;

public interface PasswordResetNotifier {

    void sendPasswordReset(AuthUser user, String rawToken);
}

package org.example.cartservice.application.port.out;

import java.util.UUID;

public interface CurrentUserProvider {

    UUID getCurrentUserId();
}

package org.example.orderservice.application.port.out;

public interface OpaqueTokenProvider {

    String generate();

    String hash(String rawToken);
}

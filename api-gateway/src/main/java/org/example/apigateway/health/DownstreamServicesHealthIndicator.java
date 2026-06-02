package org.example.apigateway.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DownstreamServicesHealthIndicator implements ReactiveHealthIndicator {

    private final WebClient webClient;
    private final String orderServiceHealthUrl;
    private final String storageServiceHealthUrl;
    private final Duration timeout;

    public DownstreamServicesHealthIndicator(
            WebClient.Builder webClientBuilder,
            @Value("${services.order.health-url}") String orderServiceHealthUrl,
            @Value("${services.storage.health-url}") String storageServiceHealthUrl,
            @Value("${services.health-timeout-millis:1000}") long healthTimeoutMillis) {
        this.webClient = webClientBuilder.build();
        this.orderServiceHealthUrl = orderServiceHealthUrl;
        this.storageServiceHealthUrl = storageServiceHealthUrl;
        this.timeout = Duration.ofMillis(healthTimeoutMillis);
    }

    @Override
    public Mono<Health> health() {
        return Mono.zip(
                        isHealthy(orderServiceHealthUrl),
                        isHealthy(storageServiceHealthUrl))
                .map(results -> buildHealth(results.getT1(), results.getT2()));
    }

    private Mono<Boolean> isHealthy(String healthUrl) {
        return webClient.get()
                .uri(healthUrl)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .timeout(timeout)
                .onErrorReturn(false);
    }

    private Health buildHealth(boolean orderServiceHealthy, boolean storageServiceHealthy) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("order-service", status(orderServiceHealthy));
        details.put("storage-service", status(storageServiceHealthy));
        return (orderServiceHealthy && storageServiceHealthy ? Health.up() : Health.down())
                .withDetails(details)
                .build();
    }

    private String status(boolean healthy) {
        return healthy ? "UP" : "DOWN";
    }
}

package org.example.paymentservice.infrastructure.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProcessedWebhookService {

    private final ProcessedWebhookRepository repository;

    public void processOnce(String providerEventId, Runnable handler) {
        if (repository.existsById(providerEventId)) {
            return;
        }
        repository.save(ProcessedWebhook.builder()
                .providerEventId(providerEventId)
                .processedAt(Instant.now())
                .build());
        handler.run();
    }
}

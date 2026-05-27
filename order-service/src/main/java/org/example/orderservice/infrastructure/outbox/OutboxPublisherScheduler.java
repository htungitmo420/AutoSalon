package org.example.orderservice.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "outbox.publisher.scheduling.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class OutboxPublisherScheduler {

    private final OutboxPublisher outboxPublisher;

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:5000}")
    public void publishPendingEvents() {
        outboxPublisher.publishPendingEvents();
    }
}

package org.example.paymentservice.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublisherScheduler {
    private final OutboxPublisher outboxPublisher;

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:1000}")
    public void publish() {
        outboxPublisher.publishPendingEvents();
    }
}

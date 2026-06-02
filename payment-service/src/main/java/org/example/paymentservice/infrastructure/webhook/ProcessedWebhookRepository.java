package org.example.paymentservice.infrastructure.webhook;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedWebhookRepository extends JpaRepository<ProcessedWebhook, String> {
}

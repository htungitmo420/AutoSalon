package org.example.paymentservice.infrastructure.webhook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "processed_webhooks", schema = "auto_salon")
public class ProcessedWebhook {
    @Id
    @Column(name = "provider_event_id")
    private String providerEventId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
}

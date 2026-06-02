package org.example.paymentservice.infrastructure.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outbox_events", schema = "auto_salon")
public class OutboxEvent {
    @Id
    @UuidGenerator
    @GeneratedValue
    private UUID id;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;
    @Column(nullable = false)
    private String topic;
    @Column(name = "message_key", nullable = false)
    private String messageKey;
    @Column(name = "payload_type", nullable = false)
    private String payloadType;
    @Column(nullable = false)
    private String payload;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;
    @Column(nullable = false)
    private int attempts;
    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;
    @Column(name = "published_at")
    private Instant publishedAt;
    @Column(name = "last_error")
    private String lastError;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = OutboxEventStatus.PENDING;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    void markPublished() {
        status = OutboxEventStatus.PUBLISHED;
        publishedAt = Instant.now();
        nextAttemptAt = null;
        lastError = null;
    }

    void markFailed(String error) {
        attempts++;
        status = OutboxEventStatus.FAILED;
        lastError = error == null ? null : error.substring(0, Math.min(error.length(), 500));
        nextAttemptAt = Instant.now().plusSeconds(Math.min(300, attempts * 10));
    }
}

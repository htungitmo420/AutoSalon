package org.example.storageservice.infrastructure.outbox;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
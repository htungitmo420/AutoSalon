CREATE SCHEMA IF NOT EXISTS auto_salon;

CREATE TABLE auto_salon.payable_references (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    target_type TEXT NOT NULL,
    reference_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    total_amount NUMERIC(19,2) NOT NULL,
    CONSTRAINT uq_payable_reference UNIQUE (target_type, reference_id)
);

CREATE TABLE auto_salon.payment_intents (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    reference_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    target_type TEXT NOT NULL,
    purpose TEXT NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency TEXT NOT NULL,
    status TEXT NOT NULL,
    idempotency_key TEXT NOT NULL UNIQUE,
    provider_payment_intent_id TEXT UNIQUE,
    client_secret TEXT,
    failure_message TEXT
);

CREATE INDEX idx_payment_intents_reference ON auto_salon.payment_intents (target_type, reference_id);

CREATE TABLE auto_salon.processed_webhooks (
    provider_event_id TEXT PRIMARY KEY NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE auto_salon.outbox_events (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    topic TEXT NOT NULL,
    message_key TEXT NOT NULL,
    payload_type TEXT NOT NULL,
    payload TEXT NOT NULL,
    status TEXT NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE,
    last_error TEXT
);

CREATE INDEX idx_payment_outbox_events_ready
    ON auto_salon.outbox_events (status, next_attempt_at, created_at);

CREATE SCHEMA IF NOT EXISTS auto_salon;

CREATE TABLE auto_salon.user_notifications (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    type VARCHAR(64) NOT NULL,
    reference_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    trace_id VARCHAR(255),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_user_notifications_customer_occurred
    ON auto_salon.user_notifications (customer_id, occurred_at DESC);
CREATE INDEX idx_user_notifications_customer_unread
    ON auto_salon.user_notifications (customer_id, read_at)
    WHERE read_at IS NULL;

CREATE TABLE auto_salon.dashboard_metrics (
    metric_key VARCHAR(255) PRIMARY KEY,
    event_count BIGINT NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE auto_salon.reference_owners (
    reference_id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    reference_type VARCHAR(64) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE auto_salon.processed_events (
    event_id UUID PRIMARY KEY,
    topic VARCHAR(255) NOT NULL,
    message_key VARCHAR(255) NOT NULL,
    trace_id VARCHAR(255),
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

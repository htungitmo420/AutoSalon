CREATE TABLE auto_salon.processed_events (
    event_id UUID PRIMARY KEY NOT NULL,
    topic TEXT NOT NULL,
    message_key TEXT NOT NULL,
    trace_id TEXT,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_processed_events_processed_at
    ON auto_salon.processed_events (processed_at);
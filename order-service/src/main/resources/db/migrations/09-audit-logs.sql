CREATE TABLE auto_salon.audit_logs (
    id UUID PRIMARY KEY NOT NULL,
    actor TEXT NOT NULL,
    action TEXT NOT NULL,
    resource TEXT NOT NULL,
    trace_id TEXT NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_order_audit_logs_occurred_at
    ON auto_salon.audit_logs (occurred_at DESC);

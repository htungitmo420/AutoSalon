CREATE TABLE auto_salon.catalog_assets (
    id UUID PRIMARY KEY,
    model_id UUID NOT NULL REFERENCES auto_salon.car_models(id),
    object_key VARCHAR(1000) NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_catalog_assets_model_status
    ON auto_salon.catalog_assets (model_id, status, created_at);

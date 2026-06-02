CREATE SCHEMA IF NOT EXISTS auto_salon;

CREATE TABLE auto_salon.carts (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    customer_id UUID NOT NULL,
    status TEXT NOT NULL,
    item_type TEXT NOT NULL,
    car_id UUID,
    model_id UUID,
    quoted_price NUMERIC(19,2) NOT NULL,
    quote_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_cart_item_reference CHECK (
        (item_type = 'STOCK_CAR' AND car_id IS NOT NULL AND model_id IS NULL)
        OR (item_type = 'CONFIGURED_CAR' AND model_id IS NOT NULL AND car_id IS NULL)
    ),
    CONSTRAINT chk_cart_quoted_price CHECK (quoted_price >= 0)
);

CREATE TABLE auto_salon.cart_selected_parts (
    cart_id UUID NOT NULL,
    part_type TEXT NOT NULL,
    part_id UUID NOT NULL,
    CONSTRAINT pk_cart_selected_parts PRIMARY KEY (cart_id, part_type),
    CONSTRAINT fk_cart_selected_parts_cart_id
        FOREIGN KEY (cart_id) REFERENCES auto_salon.carts(id)
);

CREATE UNIQUE INDEX uq_active_cart_customer_id
    ON auto_salon.carts (customer_id)
    WHERE status = 'ACTIVE' AND removed = FALSE;

CREATE INDEX idx_carts_status_expiry
    ON auto_salon.carts (status, quote_expires_at)
    WHERE removed = FALSE;

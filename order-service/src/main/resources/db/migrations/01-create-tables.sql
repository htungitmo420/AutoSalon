CREATE SCHEMA IF NOT EXISTS auto_salon;

CREATE TABLE auto_salon.common_car_orders (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    car_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    status TEXT
);

CREATE TABLE auto_salon.custom_car_orders (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    model_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    total_price NUMERIC(19,2),
    status TEXT
);

CREATE TABLE auto_salon.test_drives (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    car_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    status TEXT,
    start_date_time TIMESTAMP
);

CREATE TABLE auto_salon.custom_order_selected_parts (
    custom_order_id UUID NOT NULL,
    part_type TEXT NOT NULL,
    part_id UUID NOT NULL
);
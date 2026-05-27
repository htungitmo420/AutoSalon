CREATE SCHEMA IF NOT EXISTS auto_salon;

CREATE TABLE auto_salon.car_models (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    brand TEXT,
    model_name TEXT,
    body_type TEXT,
    fuel_type TEXT,
    engine_power INTEGER,
    engine_volume_liters DOUBLE PRECISION,
    gear_box_type TEXT,
    drivetrain_type TEXT,
    base_price NUMERIC(19,2)
);

CREATE TABLE auto_salon.cars (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    model_id UUID NOT NULL,
    color TEXT,
    price NUMERIC(19,2),
    test_drive BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE auto_salon.parts (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    type TEXT,
    name TEXT,
    surcharge NUMERIC(19,2)
);

CREATE TABLE auto_salon.car_model_base_parts (
    car_model_id UUID NOT NULL,
    part_type TEXT NOT NULL,
    part_id UUID NOT NULL
);

CREATE TABLE auto_salon.part_compatible_models (
    part_id UUID NOT NULL,
    model_id UUID NOT NULL
);

CREATE TABLE auto_salon.part_stocks (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    part_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    reserved_quantity INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE auto_salon.assembly_orders (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    source_order_id UUID NOT NULL,
    source_order_type TEXT NOT NULL,
    car_id UUID,
    model_id UUID,
    warehouse_employee_id UUID,
    status TEXT NOT NULL,
    failure_reason TEXT
);

CREATE TABLE auto_salon.assembly_order_required_parts (
    assembly_order_id UUID NOT NULL,
    part_slot TEXT NOT NULL,
    part_id UUID NOT NULL
);

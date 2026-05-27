CREATE TABLE auto_salon.inventory_reservations (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    order_id UUID NOT NULL,
    source_order_type TEXT NOT NULL,
    car_id UUID,
    model_id UUID,
    total_price NUMERIC(19,2),
    status TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    confirmed_at TIMESTAMP WITH TIME ZONE,
    release_reason TEXT
);

CREATE TABLE auto_salon.reservation_required_parts (
    reservation_id UUID NOT NULL,
    part_slot TEXT NOT NULL,
    part_id UUID NOT NULL
);

ALTER TABLE auto_salon.inventory_reservations
    ADD CONSTRAINT uq_inventory_reservations_order_id UNIQUE (order_id);

ALTER TABLE auto_salon.inventory_reservations
    ADD CONSTRAINT fk_inventory_reservations_car_id
    FOREIGN KEY (car_id) REFERENCES auto_salon.cars(id);

ALTER TABLE auto_salon.inventory_reservations
    ADD CONSTRAINT fk_inventory_reservations_model_id
    FOREIGN KEY (model_id) REFERENCES auto_salon.car_models(id);

ALTER TABLE auto_salon.reservation_required_parts
    ADD CONSTRAINT pk_reservation_required_parts PRIMARY KEY (reservation_id, part_slot);

ALTER TABLE auto_salon.reservation_required_parts
    ADD CONSTRAINT fk_reservation_parts_reservation_id
    FOREIGN KEY (reservation_id) REFERENCES auto_salon.inventory_reservations(id);

ALTER TABLE auto_salon.reservation_required_parts
    ADD CONSTRAINT fk_reservation_parts_part_id
    FOREIGN KEY (part_id) REFERENCES auto_salon.parts(id);

CREATE INDEX idx_inventory_reservations_status_expires_at
    ON auto_salon.inventory_reservations (status, expires_at);

CREATE UNIQUE INDEX uq_active_inventory_reservation_car_id
    ON auto_salon.inventory_reservations (car_id)
    WHERE car_id IS NOT NULL
      AND removed = FALSE
      AND status IN ('HELD', 'CONFIRMED', 'FULFILLED');

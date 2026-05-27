ALTER TABLE auto_salon.common_car_orders
    ADD COLUMN reservation_id UUID;

ALTER TABLE auto_salon.custom_car_orders
    ADD COLUMN reservation_id UUID;

CREATE INDEX idx_common_orders_reservation_id
    ON auto_salon.common_car_orders (reservation_id);

CREATE INDEX idx_custom_orders_reservation_id
    ON auto_salon.custom_car_orders (reservation_id);

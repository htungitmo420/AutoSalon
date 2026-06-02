ALTER TABLE auto_salon.common_car_orders
    ADD COLUMN cart_id UUID;

ALTER TABLE auto_salon.custom_car_orders
    ADD COLUMN cart_id UUID;

CREATE UNIQUE INDEX uq_common_orders_cart_id
    ON auto_salon.common_car_orders (cart_id)
    WHERE cart_id IS NOT NULL;

CREATE UNIQUE INDEX uq_custom_orders_cart_id
    ON auto_salon.custom_car_orders (cart_id)
    WHERE cart_id IS NOT NULL;

ALTER TABLE auto_salon.common_car_orders
    ADD COLUMN IF NOT EXISTS total_price NUMERIC(19,2),
    ADD COLUMN IF NOT EXISTS paid_amount NUMERIC(19,2) NOT NULL DEFAULT 0;

ALTER TABLE auto_salon.custom_car_orders
    ADD COLUMN IF NOT EXISTS paid_amount NUMERIC(19,2) NOT NULL DEFAULT 0;

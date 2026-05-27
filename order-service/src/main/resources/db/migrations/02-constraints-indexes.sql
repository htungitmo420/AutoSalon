ALTER TABLE auto_salon.custom_order_selected_parts
    ADD CONSTRAINT pk_custom_order_selected_parts
    PRIMARY KEY (custom_order_id, part_type);

ALTER TABLE auto_salon.custom_order_selected_parts
    ADD CONSTRAINT fk_selected_parts_order_id
    FOREIGN KEY (custom_order_id) REFERENCES auto_salon.custom_car_orders(id);

CREATE INDEX idx_common_orders_car_id
    ON auto_salon.common_car_orders (car_id);

CREATE INDEX idx_custom_orders_model_id
    ON auto_salon.custom_car_orders (model_id);

CREATE INDEX idx_test_drives_car_id_start
    ON auto_salon.test_drives (car_id, start_date_time);
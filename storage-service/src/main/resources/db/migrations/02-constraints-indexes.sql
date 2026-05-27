ALTER TABLE auto_salon.cars
    ADD CONSTRAINT fk_cars_model_id
    FOREIGN KEY (model_id) REFERENCES auto_salon.car_models(id);

ALTER TABLE auto_salon.car_model_base_parts
    ADD CONSTRAINT pk_car_model_base_parts
    PRIMARY KEY (car_model_id, part_type);

ALTER TABLE auto_salon.car_model_base_parts
    ADD CONSTRAINT fk_model_base_parts_model_id
    FOREIGN KEY (car_model_id) REFERENCES auto_salon.car_models(id);

ALTER TABLE auto_salon.car_model_base_parts
    ADD CONSTRAINT fk_model_base_parts_part_id
    FOREIGN KEY (part_id) REFERENCES auto_salon.parts(id);

ALTER TABLE auto_salon.part_compatible_models
    ADD CONSTRAINT pk_part_compatible_models
    PRIMARY KEY (part_id, model_id);

ALTER TABLE auto_salon.part_compatible_models
    ADD CONSTRAINT fk_compatible_models_part_id
    FOREIGN KEY (part_id) REFERENCES auto_salon.parts(id);

ALTER TABLE auto_salon.part_compatible_models
    ADD CONSTRAINT fk_compatible_models_model_id
    FOREIGN KEY (model_id) REFERENCES auto_salon.car_models(id);

ALTER TABLE auto_salon.part_stocks
    ADD CONSTRAINT fk_part_stocks_part_id
        FOREIGN KEY (part_id) REFERENCES auto_salon.parts(id);

ALTER TABLE auto_salon.part_stocks
    ADD CONSTRAINT uq_part_stocks_part_id
        UNIQUE (part_id);

ALTER TABLE auto_salon.part_stocks
    ADD CONSTRAINT chk_part_stocks_quantity
        CHECK (quantity >= 0);

ALTER TABLE auto_salon.part_stocks
    ADD CONSTRAINT chk_part_stocks_reserved_quantity
        CHECK (reserved_quantity >= 0 AND reserved_quantity <= quantity);

ALTER TABLE auto_salon.assembly_orders
    ADD CONSTRAINT fk_assembly_orders_car_id
        FOREIGN KEY (car_id) REFERENCES auto_salon.cars(id);

ALTER TABLE auto_salon.assembly_orders
    ADD CONSTRAINT fk_assembly_orders_model_id
        FOREIGN KEY (model_id) REFERENCES auto_salon.car_models(id);

ALTER TABLE auto_salon.assembly_orders
    ADD CONSTRAINT uq_assembly_orders_source_order_id
        UNIQUE (source_order_id);

ALTER TABLE auto_salon.assembly_order_required_parts
    ADD CONSTRAINT pk_assembly_order_required_parts
        PRIMARY KEY (assembly_order_id, part_slot);

ALTER TABLE auto_salon.assembly_order_required_parts
    ADD CONSTRAINT fk_assembly_required_parts_order_id
        FOREIGN KEY (assembly_order_id) REFERENCES auto_salon.assembly_orders(id);

ALTER TABLE auto_salon.assembly_order_required_parts
    ADD CONSTRAINT fk_assembly_required_parts_part_id
        FOREIGN KEY (part_id) REFERENCES auto_salon.parts(id);

CREATE INDEX idx_cars_model_id
    ON auto_salon.cars (model_id);

CREATE INDEX idx_part_stocks_part_id
    ON auto_salon.part_stocks (part_id);

CREATE INDEX idx_assembly_orders_source_order_id
    ON auto_salon.assembly_orders (source_order_id);

CREATE INDEX idx_assembly_orders_status
    ON auto_salon.assembly_orders (status);

INSERT INTO auto_salon.car_models (id, created_at, updated_at, removed, brand, model_name,
                                   body_type, fuel_type, engine_power, engine_volume_liters,
                                   gear_box_type, drivetrain_type, base_price) VALUES (
    '11111111-1111-1111-1111-111111111111',
    '2026-01-01T00:00:00',
    '2026-01-01T00:00:00',
    FALSE,
    'BMW',
    'M3',
    'SEDAN',
    'GASOLINE',
    480,
    3.0,
    'AUTOMATIC',
    'AWD',
    2000000.00);

INSERT INTO auto_salon.parts (id, created_at, updated_at, removed, type, name, surcharge) VALUES (
    '22222222-2222-2222-2222-222222222222',
    '2026-01-01T00:00:00',
    '2026-01-01T00:00:00',
    FALSE,
    'OTHER',
    'Engine 3.0 Turbo',
    120000.00);

INSERT INTO auto_salon.parts (id, created_at, updated_at, removed, type, name, surcharge) VALUES (
    '33333333-3333-3333-3333-333333333333',
    '2026-01-01T00:00:00',
    '2026-01-01T00:00:00',
    FALSE,
    'WHEELS',
    '19 inch alloy wheels',
    150000.00);

INSERT INTO auto_salon.part_compatible_models (part_id, model_id) VALUES (
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111');

INSERT INTO auto_salon.part_compatible_models (part_id, model_id) VALUES (
    '33333333-3333-3333-3333-333333333333',
    '11111111-1111-1111-1111-111111111111');

INSERT INTO auto_salon.car_model_base_parts (car_model_id, part_type, part_id) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'OTHER',
    '22222222-2222-2222-2222-222222222222');

INSERT INTO auto_salon.car_model_base_parts (car_model_id, part_type, part_id) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'WHEELS',
    '33333333-3333-3333-3333-333333333333');

INSERT INTO auto_salon.cars (id, created_at, updated_at, removed, model_id, color, price, test_drive) VALUES (
    '44444444-4444-4444-4444-444444444444',
    '2026-01-01T00:00:00',
    '2026-01-01T00:00:00',
    FALSE,
    '11111111-1111-1111-1111-111111111111',
    'BLACK',
    2500000.00,
    TRUE);

INSERT INTO auto_salon.car_models (id, created_at, updated_at, removed, brand, model_name,
                                   body_type, fuel_type, engine_power, engine_volume_liters,
                                   gear_box_type, drivetrain_type, base_price) VALUES (
    '55555555-5555-5555-5555-555555555555',
    '2026-01-01T00:00:00',
    '2026-01-01T00:00:00',
    FALSE,
    'AUDI',
    'A6',
    'SEDAN',
    'DIESEL',
    286,
    3.0,
    'AUTOMATIC',
    'AWD',
    3200000.00);

INSERT INTO auto_salon.parts (id, created_at, updated_at, removed, type, name, surcharge) VALUES (
    '66666666-6666-6666-6666-666666666666',
    '2026-01-01T00:00:00',
    '2026-01-01T00:00:00',
    FALSE,
    'INTERIOR',
    'Premium sport interior',
    220000.00);

INSERT INTO auto_salon.part_compatible_models (part_id, model_id) VALUES (
    '66666666-6666-6666-6666-666666666666',
    '55555555-5555-5555-5555-555555555555');

INSERT INTO auto_salon.car_model_base_parts (car_model_id, part_type, part_id) VALUES (
    '55555555-5555-5555-5555-555555555555',
    'INTERIOR',
    '66666666-6666-6666-6666-666666666666');

INSERT INTO auto_salon.cars (id, created_at, updated_at, removed, model_id, color, price, test_drive) VALUES (
    '77777777-7777-7777-7777-777777777777',
    '2026-01-01T00:00:00',
    '2026-01-01T00:00:00',
    FALSE,
    '55555555-5555-5555-5555-555555555555',
    'WHITE',
    3550000.00,
    FALSE);

INSERT INTO auto_salon.part_stocks (id, created_at, updated_at, removed, part_id, quantity, reserved_quantity) VALUES (
    '10101010-1010-1010-1010-101010101010',
    '2026-01-01T00:00:00',
    '2026-01-01T00:00:00',
    FALSE,
    '22222222-2222-2222-2222-222222222222',
    8,
    0);

INSERT INTO auto_salon.part_stocks (id, created_at, updated_at, removed, part_id, quantity, reserved_quantity) VALUES (
    '20202020-2020-2020-2020-202020202020',
    '2026-01-01T00:00:00',
    '2026-01-01T00:00:00',
    FALSE,
    '33333333-3333-3333-3333-333333333333',
    12,
    0);

INSERT INTO auto_salon.part_stocks (id, created_at, updated_at, removed, part_id, quantity, reserved_quantity) VALUES (
    '30303030-3030-3030-3030-303030303030',
    '2026-01-01T00:00:00',
    '2026-01-01T00:00:00',
    FALSE,
    '66666666-6666-6666-6666-666666666666',
    5,
    0);
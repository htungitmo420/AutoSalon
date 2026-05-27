INSERT INTO auto_salon.common_car_orders (id, created_at, updated_at, removed, car_id, customer_id, status) VALUES (
    '88888888-8888-8888-8888-888888888888',
    '2026-01-02T08:00:00',
    '2026-01-02T08:00:00',
    FALSE,
    '44444444-4444-4444-4444-444444444444',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'CREATED'
    );

INSERT INTO auto_salon.custom_car_orders (id, created_at, updated_at, removed, model_id, customer_id, total_price, status) VALUES (
    '99999999-9999-9999-9999-999999999999',
    '2026-01-03T09:00:00',
    '2026-01-03T09:00:00',
    FALSE,
    '55555555-5555-5555-5555-555555555555',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    67700.00,
    'CREATED'
    );

INSERT INTO auto_salon.custom_order_selected_parts (custom_order_id, part_type, part_id) VALUES (
    '99999999-9999-9999-9999-999999999999',
    'INTERIOR',
    '66666666-6666-6666-6666-666666666666'
    );

INSERT INTO auto_salon.test_drives (id, created_at, updated_at, removed, car_id, customer_id, status, start_date_time) VALUES (
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    '2026-01-04T10:00:00',
    '2026-01-04T10:00:00',
    FALSE,
    '77777777-7777-7777-7777-777777777777',
    'dddddddd-dddd-dddd-dddd-dddddddddddd',
    'PENDING',
    '2026-01-10T00:00:00'
    );
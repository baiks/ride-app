-- Insert Admin User
INSERT INTO users (
    email,
    password,
    first_name,
    last_name,
    phone_number,
    role,
    driver_status,
    vehicle_type,
    license_plate,
    active,
    created_at
) VALUES (
    'admin@ridesharingapp.com',
    '$2a$10$gYjE9nq9d7A6EcezKuXfUecf7OZCqtTb9BtT3CpdLsn5a0Z0OTVnW', -- password: Admin@123
    'System',
    'Administrator',
    '+254700000000',
    'ADMIN',
    NULL,
    NULL,
    NULL,
    true,
    CURRENT_TIMESTAMP
);


-- Insert Driver User 1
INSERT INTO users (
    email,
    password,
    first_name,
    last_name,
    phone_number,
    role,
    driver_status,
    vehicle_type,
    license_plate,
    active,
    created_at
) VALUES (
    'driver1@ridesharingapp.com',
    '$2a$10$W8x7k9v81YeK7n5u8o2bTeGLq5h7iiuD6YHkqMpJsnHKhIaHNEd2u', -- password: Driver1@123
    'John',
    'Driver',
    '+254711111111',
    'DRIVER',
    'AVAILABLE',
    'SEDAN',
    'KCA 123A',
    true,
    CURRENT_TIMESTAMP
);


-- Insert Driver User 2
INSERT INTO users (
    email,
    password,
    first_name,
    last_name,
    phone_number,
    role,
    driver_status,
    vehicle_type,
    license_plate,
    active,
    created_at
) VALUES (
    'driver2@ridesharingapp.com',
    '$2a$10$0R3uj9FqK6t1se9MumB9veI5tpup8Qpfr9Ds5h8O9Lb2ZQ9S3Vu8G', -- password: Driver2@123
    'Mary',
    'Kamau',
    '+254722222222',
    'DRIVER',
    'OFFLINE',
    'SUV',
    'KCB 456B',
    true,
    CURRENT_TIMESTAMP
);


-- Insert Customer User 1
INSERT INTO users (
    email,
    password,
    first_name,
    last_name,
    phone_number,
    role,
    driver_status,
    vehicle_type,
    license_plate,
    active,
    created_at
) VALUES (
    'customer1@example.com',
    '$2a$10$pvC3p1zE7cF6cgW7CNo08OHVNcwiuNQPNLfWyaNbUf6lCVOjBms7m', -- password: Customer1@123
    'Jane',
    'Doe',
    '+254733333333',
    'CUSTOMER',
    NULL,
    NULL,
    NULL,
    true,
    CURRENT_TIMESTAMP
);


-- Insert Customer User 2
INSERT INTO users (
    email,
    password,
    first_name,
    last_name,
    phone_number,
    role,
    driver_status,
    vehicle_type,
    license_plate,
    active,
    created_at
) VALUES (
    'customer2@example.com',
    '$2a$10$7Lk5RaeCY7EbdLD9nBSH8uRjLkH99m5kIyX7bONlUtH1pHNrZyH7u', -- password: Customer2@123
    'Peter',
    'Mwangi',
    '+254744444444',
    'CUSTOMER',
    NULL,
    NULL,
    NULL,
    true,
    CURRENT_TIMESTAMP
);

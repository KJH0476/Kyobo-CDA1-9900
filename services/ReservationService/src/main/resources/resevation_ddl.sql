CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- RestaurantAvailability 테이블
CREATE TABLE restaurant_availability (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    restaurant_id UUID NOT NULL,
    date DATE NOT NULL,
    time_slot TIME NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL CHECK (available_seats >= 0 AND available_seats <= total_seats),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (restaurant_id, date, time_slot)
);

-- Reservation 테이블
CREATE TABLE reservation (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    availability_id UUID NOT NULL REFERENCES RestaurantAvailability(id),
    reservation_time TIMESTAMP NOT NULL,
    number_of_people INT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reservation_user_id ON Reservation(user_id);

-- Waitlist 테이블
CREATE TABLE wait_list (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    availability_id UUID NOT NULL REFERENCES RestaurantAvailability(id),
    request_time TIMESTAMP NOT NULL,
    number_of_people INT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('WAITING', 'NOTIFIED', 'CONFIRMED', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_waitlist_user_id ON Waitlist(user_id);
CREATE TABLE restaurant_availability (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL,
    restaurant_name VARCHAR(100) NOT NULL,
    reservation_date DATE NOT NULL,
    reservation_time TIME NOT NULL,
    total_tables INTEGER NOT NULL,
    available_tables INTEGER NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE reservation (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL,
    user_email VARCHAR(100) NOT NULL,
    availability_id UUID NOT NULL,
    reservation_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    number_of_guests INTEGER NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_reservation_availability FOREIGN KEY (availability_id) REFERENCES restaurant_availability(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE wait_list (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL,
    user_email VARCHAR(100) NOT NULL,
    availability_id UUID NOT NULL,
    number_of_guests INTEGER NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_waitlist_availability FOREIGN KEY (availability_id) REFERENCES restaurant_availability(id) ON DELETE CASCADE ON UPDATE CASCADE
);

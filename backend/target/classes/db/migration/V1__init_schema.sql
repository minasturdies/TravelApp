-- Initial schema for the travel activity recommender app

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE destinations (
    id         BIGSERIAL PRIMARY KEY,
    city_name  VARCHAR(255) NOT NULL,
    country    VARCHAR(255),
    latitude   DOUBLE PRECISION,
    longitude  DOUBLE PRECISION,
    UNIQUE (city_name, country)
);

CREATE TABLE interests (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE activities (
    id                 BIGSERIAL PRIMARY KEY,
    destination_id     BIGINT NOT NULL REFERENCES destinations(id) ON DELETE CASCADE,
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    price_range        VARCHAR(20),         -- e.g. '$', '$$', '$$$', '$$$$'
    category           VARCHAR(100),
    rating             NUMERIC(2,1),
    external_source    VARCHAR(50),          -- e.g. 'foursquare'
    external_source_id VARCHAR(255),         -- id from the external API, for caching/dedup
    latitude           DOUBLE PRECISION,
    longitude          DOUBLE PRECISION,
    address            VARCHAR(500),
    image_url          VARCHAR(1000),
    fetched_at         TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (external_source, external_source_id)
);

CREATE INDEX idx_activities_destination ON activities(destination_id);

CREATE TABLE activity_interests (
    activity_id BIGINT NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
    interest_id BIGINT NOT NULL REFERENCES interests(id) ON DELETE CASCADE,
    PRIMARY KEY (activity_id, interest_id)
);

CREATE TABLE saved_trips (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    destination_id BIGINT NOT NULL REFERENCES destinations(id) ON DELETE CASCADE,
    start_date     DATE,
    end_date       DATE,
    budget         NUMERIC(10,2),
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_saved_trips_user ON saved_trips(user_id);

-- Seed a starter list of interests so the frontend has something to render immediately
INSERT INTO interests (name) VALUES
    ('food'), ('hiking'), ('museums'), ('nightlife'), ('art'),
    ('shopping'), ('history'), ('nature'), ('relaxation'), ('adventure');

CREATE TABLE IF NOT EXISTS categories (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(50)  NOT NULL,
    CONSTRAINT uq_category_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS events (
    id                 BIGSERIAL     PRIMARY KEY,
    created_on         TIMESTAMP     NOT NULL DEFAULT NOW(),
    title              VARCHAR(120)  NOT NULL,
    annotation         VARCHAR(2000) NOT NULL,
    description        VARCHAR(7000) NOT NULL,
    event_date         TIMESTAMP     NOT NULL,
    state              VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    paid               BOOLEAN       NOT NULL DEFAULT FALSE,
    participant_limit  INT           NOT NULL DEFAULT 0,
    request_moderation BOOLEAN       NOT NULL DEFAULT TRUE,
    initiator_id       BIGINT        NOT NULL,
    category_id        BIGINT        NOT NULL REFERENCES categories (id),
    lat                FLOAT,
    lon                FLOAT,
    published_on       TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_events_initiator_id ON events (initiator_id);
CREATE INDEX IF NOT EXISTS idx_events_state        ON events (state);
CREATE INDEX IF NOT EXISTS idx_events_category_id  ON events (category_id);

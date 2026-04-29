CREATE TABLE IF NOT EXISTS compilations (
    id     BIGSERIAL    PRIMARY KEY,
    title  VARCHAR(50)  NOT NULL,
    pinned BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS compilation_event (
    compilation_id BIGINT NOT NULL REFERENCES compilations (id) ON DELETE CASCADE,
    event_id       BIGINT NOT NULL,
    CONSTRAINT pk_compilation_event PRIMARY KEY (compilation_id, event_id)
);

CREATE TABLE IF NOT EXISTS comments (
    id         BIGSERIAL     PRIMARY KEY,
    text       VARCHAR(2000) NOT NULL,
    author_id  BIGINT        NOT NULL,
    event_id   BIGINT        NOT NULL,
    created_on TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_comments_event_id  ON comments (event_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON comments (author_id);

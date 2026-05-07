CREATE TABLE IF NOT EXISTS event_similarity (
    event_a   BIGINT  NOT NULL,
    event_b   BIGINT  NOT NULL,
    score     DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_event_similarity PRIMARY KEY (event_a, event_b)
);

CREATE INDEX IF NOT EXISTS idx_similarity_event_a ON event_similarity (event_a);
CREATE INDEX IF NOT EXISTS idx_similarity_event_b ON event_similarity (event_b);

CREATE TABLE IF NOT EXISTS user_event_interaction (
    user_id    BIGINT          NOT NULL,
    event_id   BIGINT          NOT NULL,
    weight     DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_user_event_interaction PRIMARY KEY (user_id, event_id)
);

CREATE INDEX IF NOT EXISTS idx_interaction_user_id  ON user_event_interaction (user_id);
CREATE INDEX IF NOT EXISTS idx_interaction_event_id ON user_event_interaction (event_id);

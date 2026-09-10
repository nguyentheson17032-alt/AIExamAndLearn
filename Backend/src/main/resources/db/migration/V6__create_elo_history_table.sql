CREATE TABLE elo_history (
    id           UUID PRIMARY KEY,
    user_id      UUID         NOT NULL REFERENCES users (id),
    attempt_id   UUID         REFERENCES attempts (id),
    elo_before   INTEGER      NOT NULL,
    elo_after    INTEGER      NOT NULL,
    delta        INTEGER      NOT NULL,
    reason       VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_elo_history_user_id ON elo_history (user_id);
CREATE INDEX idx_elo_history_attempt_id ON elo_history (attempt_id);
CREATE INDEX idx_elo_history_user_created ON elo_history (user_id, created_at DESC);

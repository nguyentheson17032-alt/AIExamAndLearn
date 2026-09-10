CREATE TABLE users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    elo_rating    INTEGER      NOT NULL DEFAULT 1200,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    locked        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version       BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_elo_rating ON users (elo_rating);

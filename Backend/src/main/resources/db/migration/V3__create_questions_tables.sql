CREATE TABLE questions (
    id              UUID PRIMARY KEY,
    created_by_id   UUID          NOT NULL REFERENCES users (id),
    stem            TEXT          NOT NULL,
    question_type   VARCHAR(50)   NOT NULL,
    subject         VARCHAR(100)  NOT NULL,
    topic           VARCHAR(150)  NOT NULL,
    difficulty      VARCHAR(50)   NOT NULL,
    elo_rating      INTEGER       NOT NULL DEFAULT 1200,
    bloom_level     VARCHAR(50),
    tags            TEXT,
    explanation     TEXT,
    official_answer TEXT,
    source          VARCHAR(50)   NOT NULL,
    status          VARCHAR(50)   NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    version         BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_questions_created_by_id ON questions (created_by_id);
CREATE INDEX idx_questions_subject ON questions (subject);
CREATE INDEX idx_questions_topic ON questions (topic);
CREATE INDEX idx_questions_difficulty ON questions (difficulty);
CREATE INDEX idx_questions_elo_rating ON questions (elo_rating);
CREATE INDEX idx_questions_status ON questions (status);
CREATE INDEX idx_questions_source ON questions (source);

CREATE TABLE question_choices (
    id          UUID PRIMARY KEY,
    question_id UUID         NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    label       VARCHAR(8)   NOT NULL,
    content     TEXT         NOT NULL,
    correct     BOOLEAN      NOT NULL DEFAULT FALSE,
    position    INTEGER      NOT NULL
);

CREATE INDEX idx_question_choices_question_id ON question_choices (question_id);

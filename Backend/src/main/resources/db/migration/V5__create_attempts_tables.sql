CREATE TABLE attempts (
    id            UUID PRIMARY KEY,
    user_id       UUID         NOT NULL REFERENCES users (id),
    exam_id       UUID         NOT NULL REFERENCES exams (id),
    status        VARCHAR(50)  NOT NULL,
    started_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    submitted_at  TIMESTAMPTZ,
    score         NUMERIC(8, 2),
    max_score     NUMERIC(8, 2),
    elo_before    INTEGER,
    elo_after     INTEGER,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version       BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_attempts_user_id ON attempts (user_id);
CREATE INDEX idx_attempts_exam_id ON attempts (exam_id);
CREATE INDEX idx_attempts_status ON attempts (status);
CREATE INDEX idx_attempts_user_created ON attempts (user_id, created_at DESC);

CREATE TABLE attempt_answers (
    id                   UUID PRIMARY KEY,
    attempt_id           UUID         NOT NULL REFERENCES attempts (id) ON DELETE CASCADE,
    question_id          UUID         NOT NULL REFERENCES questions (id),
    answer_text          TEXT,
    selected_choice_ids  TEXT,
    correct              BOOLEAN,
    score_awarded        NUMERIC(8, 2),
    ai_feedback          TEXT,
    UNIQUE (attempt_id, question_id)
);

CREATE INDEX idx_attempt_answers_attempt_id ON attempt_answers (attempt_id);
CREATE INDEX idx_attempt_answers_question_id ON attempt_answers (question_id);

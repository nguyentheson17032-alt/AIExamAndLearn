CREATE TABLE exams (
    id                   UUID PRIMARY KEY,
    created_by_id        UUID          NOT NULL REFERENCES users (id),
    title                VARCHAR(255)  NOT NULL,
    description          TEXT,
    exam_type            VARCHAR(50)   NOT NULL,
    source               VARCHAR(50)   NOT NULL,
    target_elo           INTEGER,
    status               VARCHAR(50)   NOT NULL,
    time_limit_minutes   INTEGER,
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    version              BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_exams_created_by_id ON exams (created_by_id);
CREATE INDEX idx_exams_exam_type ON exams (exam_type);
CREATE INDEX idx_exams_status ON exams (status);
CREATE INDEX idx_exams_target_elo ON exams (target_elo);

CREATE TABLE exam_questions (
    id          UUID PRIMARY KEY,
    exam_id     UUID    NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    question_id UUID    NOT NULL REFERENCES questions (id),
    position    INTEGER NOT NULL,
    points      INTEGER NOT NULL DEFAULT 1,
    UNIQUE (exam_id, question_id),
    UNIQUE (exam_id, position)
);

CREATE INDEX idx_exam_questions_exam_id ON exam_questions (exam_id);
CREATE INDEX idx_exam_questions_question_id ON exam_questions (question_id);

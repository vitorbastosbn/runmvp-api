CREATE TABLE running_sessions (
    id                     BIGSERIAL    PRIMARY KEY,
    creator_id             BIGINT       NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    name                   VARCHAR(80)  NOT NULL,
    mode                   VARCHAR(20)  NOT NULL,
    cooperative_goal_type  VARCHAR(20),
    target_distance_meters INTEGER      NOT NULL,
    status                 VARCHAR(20)  NOT NULL DEFAULT 'CREATED',
    premium_feature        BOOLEAN      NOT NULL DEFAULT false,
    max_duration_seconds   INTEGER      NOT NULL DEFAULT 7200,
    started_at             TIMESTAMPTZ,
    finished_at            TIMESTAMPTZ,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version                BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT ck_sessions_mode CHECK (mode IN ('COOPERATIVE','COMPETITIVE','INDIVIDUAL')),
    CONSTRAINT ck_sessions_goal_type CHECK (
        (mode = 'COOPERATIVE' AND cooperative_goal_type IN ('INDIVIDUAL_GOAL','ACCUMULATED_GOAL'))
        OR (mode <> 'COOPERATIVE' AND cooperative_goal_type IS NULL)),
    CONSTRAINT ck_sessions_status CHECK (status IN (
        'CREATED','WAITING','READY','COUNTDOWN','IN_PROGRESS',
        'FINISHING','COMPLETED','CANCELLED','INVALID')),
    CONSTRAINT ck_sessions_distance CHECK (target_distance_meters BETWEEN 500 AND 30000),
    CONSTRAINT ck_sessions_max_duration CHECK (max_duration_seconds > 0 AND max_duration_seconds <= 7200)
);

CREATE INDEX idx_sessions_status_started ON running_sessions (status, started_at);
CREATE INDEX idx_sessions_creator        ON running_sessions (creator_id, created_at DESC);
CREATE INDEX idx_sessions_finalizer      ON running_sessions (started_at)
    WHERE status = 'IN_PROGRESS';

CREATE TABLE session_participants (
    id                       BIGSERIAL   PRIMARY KEY,
    session_id               BIGINT      NOT NULL REFERENCES running_sessions (id) ON DELETE CASCADE,
    user_id                  BIGINT      NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    status                   VARCHAR(25) NOT NULL DEFAULT 'INVITED',
    elapsed_milliseconds     BIGINT,
    distance_meters          INTEGER     NOT NULL DEFAULT 0,
    official_distance_meters INTEGER,
    average_pace_sec_per_km  INTEGER,
    ranking_position         INTEGER,
    validation_status        VARCHAR(20) NOT NULL DEFAULT 'VALID',
    last_sync_at             TIMESTAMPTZ,
    finished_at              TIMESTAMPTZ,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    version                  BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uq_participants_session_user UNIQUE (session_id, user_id),
    CONSTRAINT ck_participants_status CHECK (status IN (
        'INVITED','ACCEPTED','DECLINED','REMOVED','READY','RUNNING',
        'FINISHED','ABANDONED','DISQUALIFIED','TIME_LIMIT_EXCEEDED','DISCONNECTED')),
    CONSTRAINT ck_participants_validation CHECK (validation_status IN ('VALID','UNDER_REVIEW','INVALID'))
);

CREATE INDEX idx_participants_session_status ON session_participants (session_id, status);
CREATE INDEX idx_participants_user_created   ON session_participants (user_id, created_at DESC);

CREATE TABLE location_points (
    id              BIGSERIAL        PRIMARY KEY,
    participant_id  BIGINT           NOT NULL REFERENCES session_participants (id) ON DELETE CASCADE,
    sequence        INTEGER          NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    accuracy_meters REAL             NOT NULL,
    speed_mps       REAL,
    is_mocked       BOOLEAN          NOT NULL DEFAULT false,
    captured_at     TIMESTAMPTZ      NOT NULL,
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT now(),
    CONSTRAINT uq_location_points_participant_seq UNIQUE (participant_id, sequence),
    CONSTRAINT ck_location_points_lat CHECK (latitude  BETWEEN -90  AND 90),
    CONSTRAINT ck_location_points_lng CHECK (longitude BETWEEN -180 AND 180)
);

CREATE INDEX idx_location_points_participant_captured
    ON location_points (participant_id, captured_at);

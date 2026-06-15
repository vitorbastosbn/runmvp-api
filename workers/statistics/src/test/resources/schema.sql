CREATE TABLE users (
    id             BIGSERIAL    PRIMARY KEY,
    google_subject VARCHAR(255) NOT NULL,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    avatar_url     VARCHAR(500),
    public_code    VARCHAR(12)  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ,
    CONSTRAINT uq_users_google_subject UNIQUE (google_subject),
    CONSTRAINT uq_users_public_code    UNIQUE (public_code)
);

CREATE INDEX idx_users_email ON users (email) WHERE deleted_at IS NULL;

CREATE TABLE refresh_tokens (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);

CREATE TABLE device_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    fcm_token  VARCHAR(512) NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_device_tokens_token UNIQUE (fcm_token)
);

CREATE INDEX idx_device_tokens_user ON device_tokens (user_id);
CREATE TABLE friendships (
    id           BIGSERIAL   PRIMARY KEY,
    requester_id BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    recipient_id BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_friendships_status   CHECK (status IN ('PENDING','ACCEPTED','REJECTED')),
    CONSTRAINT ck_friendships_not_self CHECK (requester_id <> recipient_id),
    CONSTRAINT uq_friendships_pair     UNIQUE (requester_id, recipient_id)
);

CREATE UNIQUE INDEX uq_friendships_pair_normalized ON friendships (
    LEAST(requester_id, recipient_id),
    GREATEST(requester_id, recipient_id)
) WHERE status IN ('PENDING','ACCEPTED');

CREATE INDEX idx_friendships_recipient_status ON friendships (recipient_id, status);
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
CREATE TABLE user_statistics (
    user_id                        BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    total_sessions                 INTEGER NOT NULL DEFAULT 0,
    completed_sessions             INTEGER NOT NULL DEFAULT 0,
    abandoned_sessions             INTEGER NOT NULL DEFAULT 0,
    official_distance_meters       BIGINT  NOT NULL DEFAULT 0,
    total_running_time_seconds     BIGINT  NOT NULL DEFAULT 0,
    average_pace_sec_per_km        INTEGER,
    longest_distance_meters        INTEGER NOT NULL DEFAULT 0,
    podiums                        INTEGER NOT NULL DEFAULT 0,
    first_places                   INTEGER NOT NULL DEFAULT 0,
    completed_cooperative_sessions INTEGER NOT NULL DEFAULT 0,
    updated_at                     TIMESTAMPTZ NOT NULL DEFAULT now(),
    version                        BIGINT  NOT NULL DEFAULT 0
);

CREATE TABLE competitive_statistics (
    user_id                   BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    victory_rate              NUMERIC(5,4) NOT NULL DEFAULT 0,
    podium_rate               NUMERIC(5,4) NOT NULL DEFAULT 0,
    best_times_by_distance    JSONB        NOT NULL DEFAULT '{}',
    podium_history_count      INTEGER      NOT NULL DEFAULT 0,
    first_place_history_count INTEGER      NOT NULL DEFAULT 0,
    current_podium_streak     INTEGER      NOT NULL DEFAULT 0,
    best_podium_streak        INTEGER      NOT NULL DEFAULT 0,
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version                   BIGINT       NOT NULL DEFAULT 0
);
CREATE TABLE subscriptions (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider         VARCHAR(20)  NOT NULL DEFAULT 'GOOGLE_PLAY',
    product_id       VARCHAR(100) NOT NULL,
    purchase_token   VARCHAR(512) NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    entitlement      VARCHAR(20)  NOT NULL,
    started_at       TIMESTAMPTZ  NOT NULL,
    expires_at       TIMESTAMPTZ,
    auto_renewing    BOOLEAN      NOT NULL DEFAULT false,
    last_verified_at TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version          BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_subscriptions_purchase_token UNIQUE (purchase_token),
    CONSTRAINT ck_subscriptions_status CHECK (status IN (
        'ACTIVE','GRACE_PERIOD','PAUSED','CANCELLED','EXPIRED','REVOKED')),
    CONSTRAINT ck_subscriptions_entitlement CHECK (entitlement IN ('FREE','PREMIUM_ACTIVE'))
);

CREATE INDEX idx_subscriptions_user_status ON subscriptions (user_id, status);
CREATE INDEX idx_subscriptions_expires     ON subscriptions (expires_at)
    WHERE status IN ('ACTIVE','GRACE_PERIOD','CANCELLED');

CREATE TABLE play_billing_events (
    id                BIGSERIAL    PRIMARY KEY,
    message_id        VARCHAR(255) NOT NULL,
    purchase_token    VARCHAR(512),
    notification_type INTEGER,
    payload           JSONB        NOT NULL,
    received_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    processed_at      TIMESTAMPTZ,
    processing_error  VARCHAR(500),
    CONSTRAINT uq_play_billing_events_message UNIQUE (message_id)
);

CREATE INDEX idx_play_billing_events_pending ON play_billing_events (received_at)
    WHERE processed_at IS NULL;

CREATE TABLE ad_profiles (
    user_id                    BIGINT      PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    ads_enabled                BOOLEAN     NOT NULL DEFAULT true,
    last_interstitial_at       TIMESTAMPTZ,
    interstitial_count_today   INTEGER     NOT NULL DEFAULT 0,
    rewarded_unlock_expires_at TIMESTAMPTZ
);
CREATE TABLE outbox_events (
    id             BIGSERIAL    PRIMARY KEY,
    aggregate_type VARCHAR(40)  NOT NULL,
    aggregate_id   BIGINT       NOT NULL,
    event_type     VARCHAR(60)  NOT NULL,
    payload        JSONB        NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    processed_at   TIMESTAMPTZ,
    processed_by   VARCHAR(60),
    attempts       INTEGER      NOT NULL DEFAULT 0,
    last_error     VARCHAR(500)
);

CREATE INDEX idx_outbox_pending ON outbox_events (event_type, created_at)
    WHERE processed_at IS NULL;
-- Plans 10-14 implement a simplified session domain (decision: follow plans, adapt schema).
-- running_sessions: PENDING/ACTIVE/COMPLETED/ABANDONED lifecycle, scheduling, no name/goal-type usage.

ALTER TABLE running_sessions ALTER COLUMN name DROP NOT NULL;
ALTER TABLE running_sessions ALTER COLUMN target_distance_meters DROP NOT NULL;
ALTER TABLE running_sessions ALTER COLUMN target_distance_meters TYPE BIGINT;
ALTER TABLE running_sessions DROP CONSTRAINT ck_sessions_distance;
ALTER TABLE running_sessions DROP CONSTRAINT ck_sessions_goal_type;
ALTER TABLE running_sessions DROP CONSTRAINT ck_sessions_status;
ALTER TABLE running_sessions ADD CONSTRAINT ck_sessions_status
    CHECK (status IN ('PENDING','ACTIVE','COMPLETED','ABANDONED'));
ALTER TABLE running_sessions ALTER COLUMN status SET DEFAULT 'PENDING';
ALTER TABLE running_sessions ADD COLUMN scheduled_at TIMESTAMPTZ;
ALTER TABLE running_sessions ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

-- session_participants: creator/guest roles and final results in domain terms.

ALTER TABLE session_participants ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'GUEST';
ALTER TABLE session_participants ADD CONSTRAINT ck_participants_role
    CHECK (role IN ('CREATOR','GUEST'));
ALTER TABLE session_participants ADD COLUMN final_position INTEGER;
ALTER TABLE session_participants ADD COLUMN running_time_seconds BIGINT;
ALTER TABLE session_participants ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE session_participants ALTER COLUMN official_distance_meters TYPE BIGINT;
ALTER TABLE subscriptions
    ADD COLUMN product_type VARCHAR(20) NOT NULL DEFAULT 'MONTHLY';

ALTER TABLE subscriptions
    DROP CONSTRAINT ck_subscriptions_status,
    ADD CONSTRAINT ck_subscriptions_status CHECK (status IN (
        'ACTIVE','GRACE_PERIOD','PAUSED','CANCELLED','EXPIRED','REVOKED','ON_HOLD'));

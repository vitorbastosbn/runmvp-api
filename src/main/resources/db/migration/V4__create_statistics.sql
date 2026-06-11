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

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

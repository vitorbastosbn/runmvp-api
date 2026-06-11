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

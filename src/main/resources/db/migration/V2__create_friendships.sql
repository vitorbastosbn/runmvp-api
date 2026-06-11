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

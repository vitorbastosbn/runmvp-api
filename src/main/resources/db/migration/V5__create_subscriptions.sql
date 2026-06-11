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

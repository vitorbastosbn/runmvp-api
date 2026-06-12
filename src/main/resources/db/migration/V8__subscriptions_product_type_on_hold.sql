ALTER TABLE subscriptions
    ADD COLUMN product_type VARCHAR(20) NOT NULL DEFAULT 'MONTHLY';

ALTER TABLE subscriptions
    DROP CONSTRAINT ck_subscriptions_status,
    ADD CONSTRAINT ck_subscriptions_status CHECK (status IN (
        'ACTIVE','GRACE_PERIOD','PAUSED','CANCELLED','EXPIRED','REVOKED','ON_HOLD'));

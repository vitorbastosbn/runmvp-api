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

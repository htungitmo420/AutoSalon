CREATE TABLE auto_salon.auth_refresh_tokens (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    user_id UUID NOT NULL,
    family_id UUID NOT NULL,
    token_hash TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    replaced_by_token_hash TEXT,
    CONSTRAINT fk_auth_refresh_tokens_user_id
        FOREIGN KEY (user_id) REFERENCES auto_salon.auth_users(id),
    CONSTRAINT uq_auth_refresh_tokens_token_hash UNIQUE (token_hash)
);

CREATE INDEX ix_auth_refresh_tokens_family_id
    ON auto_salon.auth_refresh_tokens (family_id);

CREATE INDEX ix_auth_refresh_tokens_user_id
    ON auto_salon.auth_refresh_tokens (user_id);

CREATE TABLE auto_salon.auth_password_reset_tokens (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    user_id UUID NOT NULL,
    token_hash TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_auth_password_reset_tokens_user_id
        FOREIGN KEY (user_id) REFERENCES auto_salon.auth_users(id),
    CONSTRAINT uq_auth_password_reset_tokens_token_hash UNIQUE (token_hash)
);

CREATE INDEX ix_auth_password_reset_tokens_user_id
    ON auto_salon.auth_password_reset_tokens (user_id);

CREATE TABLE auto_salon.auth_rate_limits (
    id UUID PRIMARY KEY NOT NULL,
    action TEXT NOT NULL,
    key_hash TEXT NOT NULL,
    attempts INTEGER NOT NULL,
    window_started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    blocked_until TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_auth_rate_limits_action_key UNIQUE (action, key_hash)
);

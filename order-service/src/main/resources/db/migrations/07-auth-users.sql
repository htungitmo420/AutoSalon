CREATE TABLE auto_salon.auth_users (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    email TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    full_name TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX uq_auth_users_email_active
    ON auto_salon.auth_users (LOWER(email))
    WHERE removed = FALSE;

CREATE TABLE auto_salon.auth_user_roles (
    user_id UUID NOT NULL,
    role TEXT NOT NULL
);

ALTER TABLE auto_salon.auth_user_roles
    ADD CONSTRAINT pk_auth_user_roles PRIMARY KEY (user_id, role);

ALTER TABLE auto_salon.auth_user_roles
    ADD CONSTRAINT fk_auth_user_roles_user_id
    FOREIGN KEY (user_id) REFERENCES auto_salon.auth_users(id);

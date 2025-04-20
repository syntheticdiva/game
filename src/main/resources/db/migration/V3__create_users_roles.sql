CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    roles user_role NOT NULL,
    PRIMARY KEY (user_id, roles)
);
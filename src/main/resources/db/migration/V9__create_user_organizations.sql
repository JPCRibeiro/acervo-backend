CREATE TABLE user_organizations (
    id               UUID         PRIMARY KEY,
    user_id          UUID         NOT NULL REFERENCES users (id),
    organization_id  UUID         NOT NULL REFERENCES organizations (id),
    role             VARCHAR(20)  NOT NULL,
    joined_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    last_accessed_at TIMESTAMP(6) WITH TIME ZONE,
    UNIQUE (user_id, organization_id)
);

CREATE INDEX idx_user_organizations_organization_id ON user_organizations (organization_id);

INSERT INTO user_organizations (id, user_id, organization_id, role, joined_at)
SELECT gen_random_uuid(), id, organization_id, role, created_at
FROM users;

ALTER TABLE users DROP COLUMN organization_id;
ALTER TABLE users DROP COLUMN role;

DROP INDEX IF EXISTS idx_users_organization_id;
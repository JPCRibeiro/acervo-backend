CREATE TABLE organizations (
    id          UUID         PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    invite_code VARCHAR(32)  NOT NULL,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_organizations_invite_code ON organizations (invite_code);
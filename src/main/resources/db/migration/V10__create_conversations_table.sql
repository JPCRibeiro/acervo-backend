CREATE TABLE conversations (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    user_id         UUID NOT NULL REFERENCES users (id),
    title           VARCHAR(200) NOT NULL,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_conversations_org_user ON conversations (organization_id, user_id, updated_at DESC);

CREATE TABLE messages (
    id              UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL,
    content         TEXT NOT NULL,
    sources         JSONB,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_messages_conversation ON messages (conversation_id, created_at);
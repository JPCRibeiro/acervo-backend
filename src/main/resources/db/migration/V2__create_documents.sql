CREATE TABLE documents (
    id             UUID         PRIMARY KEY,
    tenant_id      UUID         NOT NULL,
    file_name      VARCHAR(255) NOT NULL,
    s3_key         VARCHAR(255) NOT NULL,
    status         VARCHAR(255) NOT NULL,
    chunk_count    INTEGER      NOT NULL,
    failure_reason VARCHAR(255),
    uploaded_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_documents_tenant_id ON documents (tenant_id);
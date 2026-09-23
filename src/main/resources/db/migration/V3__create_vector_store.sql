CREATE TABLE vector_store (
    id        UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    content   TEXT,
    metadata  JSON,
    embedding VECTOR(1536)
);

CREATE INDEX idx_vector_store_embedding
    ON vector_store USING HNSW (embedding vector_cosine_ops);
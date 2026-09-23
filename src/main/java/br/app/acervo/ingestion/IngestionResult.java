package br.app.acervo.ingestion;

import java.util.UUID;

public record IngestionResult(
        UUID documentId,
        String s3Key,
        int chunkCount
) {}
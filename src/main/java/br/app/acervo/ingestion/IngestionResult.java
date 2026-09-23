package br.app.acervo.ingestion;

public record IngestionResult(
        String s3Key,
        int chunkCount
) {}
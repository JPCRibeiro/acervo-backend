package br.app.acervo.ingestion;

public record IngestionResponse(
        String fileName,
        int chunksIndexed
) {}
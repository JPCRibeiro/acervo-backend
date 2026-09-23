package br.app.acervo.ingestion;

import java.util.UUID;

public record IngestionResponse(
        UUID documentId,
        String fileName,
        int chunksIndexed
) {}
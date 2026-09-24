package br.app.acervo.ingestion;

import br.app.acervo.document.domain.DocumentStatus;

import java.util.UUID;

public record IngestionResponse(
        UUID documentId,
        String fileName,
        DocumentStatus status
) {}
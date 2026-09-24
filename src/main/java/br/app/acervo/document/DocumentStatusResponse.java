package br.app.acervo.document;

import br.app.acervo.document.domain.DocumentStatus;

import java.util.UUID;

public record DocumentStatusResponse(
        UUID id,
        String fileName,
        DocumentStatus status,
        int chunkCount,
        String failureReason
) {}
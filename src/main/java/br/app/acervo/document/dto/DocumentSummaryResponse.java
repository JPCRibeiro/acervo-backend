package br.app.acervo.document.dto;

import br.app.acervo.document.domain.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentSummaryResponse(
        UUID id,
        String fileName,
        DocumentStatus status,
        int chunkCount,
        long fileSizeBytes,
        Instant uploadedAt,
        String failureReason
) {}

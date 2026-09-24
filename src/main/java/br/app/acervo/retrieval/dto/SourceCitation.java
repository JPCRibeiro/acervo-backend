package br.app.acervo.retrieval.dto;

import java.util.UUID;

public record SourceCitation(
        UUID documentId,
        String fileName,
        String url
) {}
package br.app.acervo.retrieval.dto;

import java.util.UUID;

public record ChatRequest(
        String question,
        UUID tenantId
) {}

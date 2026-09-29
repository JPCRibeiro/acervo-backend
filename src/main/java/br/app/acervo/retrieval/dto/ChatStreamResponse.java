package br.app.acervo.retrieval.dto;

import java.util.List;
import java.util.UUID;

public record ChatStreamResponse(
        UUID conversationId,
        String textDelta,
        List<SourceCitation> sources
) {}
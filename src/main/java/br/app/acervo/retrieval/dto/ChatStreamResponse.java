package br.app.acervo.retrieval.dto;

import java.util.List;

public record ChatStreamResponse(
        String textDelta,
        List<SourceCitation> sources
) {}
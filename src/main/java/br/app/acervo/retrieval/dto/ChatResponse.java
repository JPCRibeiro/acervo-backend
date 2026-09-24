package br.app.acervo.retrieval.dto;

import java.util.List;

public record ChatResponse(
        String answer,
        List<SourceCitation> sources
) {}
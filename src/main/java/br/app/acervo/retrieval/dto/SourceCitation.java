package br.app.acervo.retrieval.dto;

import java.util.List;
import java.util.UUID;

public record SourceCitation(
        UUID documentId,
        String fileName,
        String url,
        double topScore,
        List<Snippet> snippets
) {
    public record Snippet(
            String text,
            Integer page,
            double score
    ) {}
}
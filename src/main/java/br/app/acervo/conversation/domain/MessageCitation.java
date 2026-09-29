package br.app.acervo.conversation.domain;

import java.util.List;
import java.util.UUID;

public record MessageCitation(
        UUID documentId,
        String fileName,
        double topScore,
        List<Snippet> snippets
) {
    public record Snippet(
            String text,
            Integer page,
            double score
    ) {}
}
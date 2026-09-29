package br.app.acervo.conversation.dto;

import br.app.acervo.conversation.domain.MessageRole;
import br.app.acervo.retrieval.dto.SourceCitation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        MessageRole role,
        String content,
        List<SourceCitation> sources,
        Instant createdAt
) {}
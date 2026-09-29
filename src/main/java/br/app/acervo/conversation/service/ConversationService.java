package br.app.acervo.conversation.service;

import br.app.acervo.conversation.domain.Conversation;
import br.app.acervo.conversation.domain.Message;
import br.app.acervo.conversation.domain.MessageCitation;
import br.app.acervo.conversation.dto.ConversationDetailResponse;
import br.app.acervo.conversation.dto.ConversationSummaryResponse;
import br.app.acervo.conversation.dto.MessageResponse;
import br.app.acervo.conversation.exception.ConversationNotFoundException;
import br.app.acervo.conversation.repository.ConversationRepository;
import br.app.acervo.conversation.repository.MessageRepository;
import br.app.acervo.document.domain.Document;
import br.app.acervo.document.repository.DocumentRepository;
import br.app.acervo.ingestion.service.StorageService;
import br.app.acervo.retrieval.dto.SourceCitation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final DocumentRepository documentRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> list(UUID organizationId, UUID userId) {
        return conversationRepository
                .findByOrganizationIdAndUserIdOrderByUpdatedAtDesc(organizationId, userId)
                .stream()
                .map(c -> new ConversationSummaryResponse(c.getId(), c.getTitle(), c.getUpdatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationDetailResponse get(UUID id, UUID organizationId, UUID userId) {
        Conversation conversation = conversationRepository
                .findByIdAndOrganizationIdAndUserId(id, organizationId, userId)
                .orElseThrow(ConversationNotFoundException::new);

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(id);
        Map<UUID, String> keyByDocumentId = resolvePresignableKeys(organizationId, messages);

        List<MessageResponse> messageResponses = messages.stream()
                .map(m -> new MessageResponse(
                        m.getId(),
                        m.getRole(),
                        m.getContent(),
                        toCitations(m.getSources(), keyByDocumentId),
                        m.getCreatedAt()))
                .toList();

        return new ConversationDetailResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messageResponses);
    }

    private Map<UUID, String> resolvePresignableKeys(UUID organizationId, List<Message> messages) {
        Set<UUID> documentIds = messages.stream()
                .map(Message::getSources)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(MessageCitation::documentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (documentIds.isEmpty()) {
            return Map.of();
        }

        return documentRepository.findAllById(documentIds).stream()
                .filter(d -> d.getOrganizationId().equals(organizationId))
                .collect(Collectors.toMap(Document::getId, Document::getS3Key));
    }

    private List<SourceCitation> toCitations(List<MessageCitation> stored, Map<UUID, String> keyByDocumentId) {
        if (stored == null || stored.isEmpty()) {
            return List.of();
        }

        return stored.stream()
                .map(c -> new SourceCitation(
                        c.documentId(),
                        c.fileName(),
                        presign(keyByDocumentId.get(c.documentId())),
                        c.topScore(),
                        c.snippets().stream()
                                .map(s -> new SourceCitation.Snippet(s.text(), s.page(), s.score()))
                                .toList()))
                .toList();
    }

    private String presign(String s3Key) {
        return s3Key == null ? null : storageService.generatePresignedUrl(s3Key);
    }
}

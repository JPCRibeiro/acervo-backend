package br.app.acervo.retrieval.service;

import br.app.acervo.conversation.domain.MessageCitation;
import br.app.acervo.conversation.service.ConversationService;
import br.app.acervo.retrieval.dto.ChatStreamResponse;
import br.app.acervo.retrieval.dto.SourceCitation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RetrievalService retrievalService;
    private final ConversationService conversationService;

    public Flux<ChatStreamResponse> streamAndPersist(UUID organizationId, UUID userId,
                                                     UUID conversationId, String question) {
        return Mono.fromCallable(() -> conversationId == null
                        ? conversationService.start(organizationId, userId, question)
                        : conversationService.appendUserMessage(conversationId, organizationId, userId, question))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(conversation -> streamAnswer(organizationId, conversation, question));
    }

    private Flux<ChatStreamResponse> streamAnswer(UUID organizationId, UUID conversation, String question) {
        StringBuilder answer = new StringBuilder();
        AtomicReference<List<SourceCitation>> citations = new AtomicReference<>(List.of());
        AtomicBoolean firstEmitted = new AtomicBoolean(true);

        Flux<ChatStreamResponse> tokens = retrievalService.askStream(organizationId, question)
                .map(chunk -> {
                    if (chunk.textDelta() != null) {
                        answer.append(chunk.textDelta());
                    }
                    if (chunk.sources() != null) {
                        citations.set(chunk.sources());
                    }
                    UUID idForClient = firstEmitted.compareAndSet(true, false) ? conversation : null;
                    return new ChatStreamResponse(idForClient, chunk.textDelta(), chunk.sources());
                });

        Mono<ChatStreamResponse> persist = Mono.<ChatStreamResponse>fromRunnable(() -> {
                    if (!answer.isEmpty()) {
                        conversationService.appendAssistantMessage(
                                conversation, answer.toString(), toMessageCitations(citations.get()));
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());

        return tokens.concatWith(persist);
    }

    private List<MessageCitation> toMessageCitations(List<SourceCitation> sources) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        return sources.stream()
                .map(s -> new MessageCitation(
                        s.documentId(),
                        s.fileName(),
                        s.topScore(),
                        s.snippets().stream()
                                .map(sn -> new MessageCitation.Snippet(sn.text(), sn.page(), sn.score()))
                                .toList()))
                .toList();
    }
}
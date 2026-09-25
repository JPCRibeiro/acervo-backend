package br.app.acervo.retrieval.service;

import br.app.acervo.ingestion.service.StorageService;
import br.app.acervo.retrieval.dto.ChatResponse;
import br.app.acervo.retrieval.dto.ChatStreamResponse;
import br.app.acervo.retrieval.dto.SourceCitation;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RetrievalService {
    private static final int TOP_K = 5;
    private static final double SIMILARITY_THRESHOLD = 0.2;

    private final ChatClient ragChatClient;
    private final VectorStore vectorStore;
    private final StorageService storageService;

    private record TempSource(UUID documentId, String fileName, String s3Key) {}
    private record Prepared(String context, List<SourceCitation> citations) {}

    public ChatResponse ask(UUID organizationId, String question) {
        Prepared prepared = prepare(organizationId, question);

        String answer = ragChatClient.prompt()
                .user(userSpec(question, prepared.context()))
                .call()
                .content();

        return new ChatResponse(answer, prepared.citations());
    }

    public Flux<ChatStreamResponse> askStream(UUID organizationId, String question) {
        Prepared prepared = prepare(organizationId, question);
        AtomicBoolean isFirst = new AtomicBoolean(true);

        return ragChatClient.prompt()
                .user(userSpec(question, prepared.context()))
                .stream()
                .content()
                .map(token -> new ChatStreamResponse(
                        token,
                        isFirst.compareAndSet(true, false) ? prepared.citations() : null
                ));
    }

    private Prepared prepare(UUID organizationId, String question) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .filterExpression("organizationId == '" + organizationId + "'")
                .topK(TOP_K)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();

        List<Document> retrieved = vectorStore.similaritySearch(searchRequest);

        String context = retrieved.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        return new Prepared(context, buildCitations(retrieved));
    }

    private Consumer<ChatClient.PromptUserSpec> userSpec(String question, String context) {
        return u -> u.text("{question}\n\nContexto dos documentos:\n{context}")
                .param("question", question)
                .param("context", context);
    }

    List<SourceCitation> buildCitations(List<Document> retrieved) {
        if (retrieved.isEmpty()) {
            return List.of();
        }

        return retrieved.stream()
                .map(doc -> {
                    var meta = doc.getMetadata();
                    UUID documentId = Optional.ofNullable(meta.get("documentId"))
                            .map(Object::toString)
                            .map(UUID::fromString)
                            .orElse(null);
                    String fileName = Optional.ofNullable(meta.get("source"))
                            .map(Object::toString)
                            .orElse("unknown");
                    String s3Key = Optional.ofNullable(meta.get("s3Key"))
                            .map(Object::toString)
                            .orElse(null);
                    return new TempSource(documentId, fileName, s3Key);
                })
                .distinct()
                .map(temp -> {
                    String url = (temp.s3Key() != null) ? storageService.generatePresignedUrl(temp.s3Key()) : null;
                    return new SourceCitation(temp.documentId(), temp.fileName(), url);
                })
                .toList();
    }
}
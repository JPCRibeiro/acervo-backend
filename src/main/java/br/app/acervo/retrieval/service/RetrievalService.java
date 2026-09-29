package br.app.acervo.retrieval.service;

import br.app.acervo.document.domain.DocumentStatus;
import br.app.acervo.document.dto.DocumentSummaryResponse;
import br.app.acervo.document.service.DocumentService;
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

import java.util.*;
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
    private final DocumentService documentService;

    private record Prepared(String inventory, String context, List<SourceCitation> citations) {}

    public ChatResponse ask(UUID organizationId, String question) {
        Prepared prepared = prepare(organizationId, question);

        String answer = ragChatClient.prompt()
                .user(userSpec(question, prepared.inventory(), prepared.context()))
                .call()
                .content();

        return new ChatResponse(answer, prepared.citations());
    }

    public Flux<ChatStreamResponse> askStream(UUID organizationId, String question) {
        Prepared prepared = prepare(organizationId, question);
        AtomicBoolean isFirst = new AtomicBoolean(true);

        return ragChatClient.prompt()
                .user(userSpec(question, prepared.inventory(), prepared.context()))
                .stream()
                .content()
                .map(token -> new ChatStreamResponse(
                        null,
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
                .map(doc -> "[" + fileName(doc) + "] " + doc.getText())
                .collect(Collectors.joining("\n\n"));

        return new Prepared(buildInventory(organizationId), context, buildCitations(retrieved));
    }

    private String buildInventory(UUID organizationId) {
        List<DocumentSummaryResponse> docs = documentService.listByOrganization(organizationId);
        if (docs.isEmpty()) {
            return "INVENTÁRIO DE DOCUMENTOS: (nenhum documento disponível)";
        }
        String list = docs.stream()
                .map(d -> "- " + d.fileName() + " (" + statusLabel(d.status()) + ")")
                .collect(Collectors.joining("\n"));
        return "INVENTÁRIO DE DOCUMENTOS (" + docs.size() + "):\n" + list;
    }

    private String statusLabel(DocumentStatus status) {
        return switch (status) {
            case READY -> "pronto";
            case PROCESSING -> "processando";
            case PENDING -> "na fila";
            case FAILED -> "falhou";
        };
    }

    private Consumer<ChatClient.PromptUserSpec> userSpec(String question, String inventory, String context) {
        return u -> u.text("""
                Pergunta: {question}

                {inventory}

                TRECHOS RELEVANTES DOS DOCUMENTOS:
                {context}
                """)
                .param("question", question)
                .param("inventory", inventory)
                .param("context", context);
    }

    List<SourceCitation> buildCitations(List<Document> retrieved) {
        if (retrieved.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<Document>> byDocument = retrieved.stream()
                .filter(doc -> documentId(doc) != null)
                .collect(Collectors.groupingBy(
                        this::documentId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return byDocument.entrySet().stream()
                .map(entry -> {
                    List<Document> chunks = entry.getValue();
                    Document first = chunks.getFirst();

                    List<SourceCitation.Snippet> snippets = chunks.stream()
                            .map(doc -> new SourceCitation.Snippet(doc.getText(), page(doc), score(doc)))
                            .sorted(Comparator.comparingDouble(SourceCitation.Snippet::score).reversed())
                            .toList();

                    double topScore = snippets.isEmpty() ? 0.0 : snippets.getFirst().score();

                    String url = Optional.ofNullable(s3Key(first))
                            .map(storageService::generatePresignedUrl)
                            .orElse(null);

                    return new SourceCitation(entry.getKey(), fileName(first), url, topScore, snippets);
                })
                .sorted(Comparator.comparingDouble(SourceCitation::topScore).reversed())
                .toList();
    }

    private UUID documentId(Document doc) {
        return Optional.ofNullable(doc.getMetadata().get("documentId"))
                .map(Object::toString)
                .map(UUID::fromString)
                .orElse(null);
    }

    private String fileName(Document doc) {
        return Optional.ofNullable(doc.getMetadata().get("source"))
                .map(Object::toString)
                .orElse("unknown");
    }

    private String s3Key(Document doc) {
        return Optional.ofNullable(doc.getMetadata().get("s3Key"))
                .map(Object::toString)
                .orElse(null);
    }

    private Integer page(Document doc) {
        return Optional.ofNullable(doc.getMetadata().get("page_number"))
                .map(Object::toString)
                .map(Integer::valueOf)
                .orElse(null);
    }

    private double score(Document doc) {
        Double s = doc.getScore();
        return s != null ? s : 0.0;
    }
}
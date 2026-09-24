package br.app.acervo.retrieval.service;

import br.app.acervo.ingestion.service.StorageService;
import br.app.acervo.retrieval.dto.ChatResponse;
import br.app.acervo.retrieval.dto.SourceCitation;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RetrievalService {
    private final ChatClient ragChatClient;
    private final VectorStore vectorStore;
    private final StorageService storageService;

    private record TempSource(UUID documentId, String fileName, String s3Key) {}

    public ChatResponse ask(UUID tenantId, String question) {
        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .filterExpression("tenantId == '" + tenantId + "'")
                        .topK(5)
                        .similarityThreshold(0.2)
                        .build())
                .build();

        var response = ragChatClient.prompt()
                .advisors(qaAdvisor)
                .user(question)
                .call()
                .chatResponse();

        String answer = response.getResult().getOutput().getText();

        List<Document> retrieved = response.getMetadata()
                .get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);

        if (retrieved == null || retrieved.isEmpty()) {
            return new ChatResponse(answer, List.of());
        }

        List<SourceCitation> sources = retrieved.stream()
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
                            .map(Object::toString).orElse(null);
                    return new TempSource(documentId, fileName, s3Key);
                })
                .distinct()
                .map(temp -> {
                    String url = (temp.s3Key != null) ? storageService.generatePresignedUrl(temp.s3Key) : null;
                    return new SourceCitation(temp.documentId(), temp.fileName(), url);
                })
                .toList();

        return new ChatResponse(answer, sources);
    }
}

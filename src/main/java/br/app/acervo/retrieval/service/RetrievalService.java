package br.app.acervo.retrieval.service;

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

    public ChatResponse ask(UUID tenantId, String question) {
        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .filterExpression("tenantId == '" + tenantId + "'")
                        .topK(5)
                        .similarityThreshold(0.3)
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
                    return new SourceCitation(documentId, fileName);
                })
                .distinct()
                .toList();

        return new ChatResponse(answer, sources);
    }
}

package br.app.acervo.retrieval.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RetrievalService {
    private final ChatClient ragChatClient;
    private final VectorStore vectorStore;

    public String ask(UUID tenantId, String question) {
        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .filterExpression("tenantId == '" + tenantId + "'")
                        .topK(5)
                        .similarityThreshold(0.3)
                        .build())
                .build();

        return ragChatClient.prompt()
                .advisors(qaAdvisor)
                .user(question)
                .call()
                .content();
    }
}

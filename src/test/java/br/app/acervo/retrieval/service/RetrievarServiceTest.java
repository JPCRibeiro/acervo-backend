package br.app.acervo.retrieval.service;

import br.app.acervo.ingestion.service.StorageService;
import br.app.acervo.retrieval.dto.SourceCitation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RetrievarServiceTest {
    @Mock
    ChatClient ragChatClient;

    @Mock
    VectorStore vectorStore;

    @Mock
    StorageService storageService;

    @InjectMocks
    RetrievalService retrievalService;

    @Test
    void deduplicatesChunksFromSameDocumentAndGeneratesUrl() {
        UUID docId = UUID.randomUUID();

        var chunk1 = Document.builder()
                .text("pedaço 1")
                .metadata(Map.of("documentId", docId.toString(), "source", "cv.pdf", "s3Key", "key1"))
                .build();
        var chunk2 = Document.builder()
                .text("pedaço 2")
                .metadata(Map.of("documentId", docId.toString(), "source", "cv.pdf", "s3Key", "key1"))
                .build();

        when(storageService.generatePresignedUrl("key1")).thenReturn("https://signed-url");

        List<SourceCitation> citations = retrievalService.buildCitations(List.of(chunk1, chunk2));

        assertThat(citations).hasSize(1);
        assertThat(citations.getFirst().documentId()).isEqualTo(docId);
        assertThat(citations.getFirst().fileName()).isEqualTo("cv.pdf");
        assertThat(citations.getFirst().url()).isEqualTo("https://signed-url");

        verify(storageService, times(1)).generatePresignedUrl("key1");
    }

    @Test
    void returnsEmptyWhenNoDocumentsRetrieved() {
        assertThat(retrievalService.buildCitations(List.of())).isEmpty();
        assertThat(retrievalService.buildCitations(List.of())).isEmpty();
    }
}

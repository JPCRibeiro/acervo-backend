package br.app.acervo;

import br.app.acervo.document.domain.DocumentStatus;
import br.app.acervo.document.repository.DocumentRepository;
import br.app.acervo.ingestion.service.DocumentProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentProcessorTest {

    @Mock VectorStore vectorStore;
    @Mock DocumentRepository documentRepository;

    @InjectMocks
    DocumentProcessor documentProcessor;

    private br.app.acervo.document.domain.Document pendingDocument(UUID id, UUID tenantId) {
        var doc = br.app.acervo.document.domain.Document.create(tenantId, "notas.txt", "fake-s3-key");
        ReflectionTestUtils.setField(doc, "id", id);
        return doc;
    }

    @Test
    void shouldEnrichMetadataAndMarkReady() {
        UUID docId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        var document = pendingDocument(docId, tenantId);
        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));

        documentProcessor.process(docId, tenantId,
                "conteúdo de teste".getBytes(), "notas.txt", "fake-s3-key");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).write(captor.capture());

        assertThat(captor.getValue()).allSatisfy(chunk -> {
            assertThat(chunk.getMetadata()).containsEntry("tenantId", tenantId.toString());
            assertThat(chunk.getMetadata()).containsEntry("source", "notas.txt");
            assertThat(chunk.getMetadata()).containsEntry("s3Key", "fake-s3-key");
            assertThat(chunk.getMetadata()).containsEntry("documentId", docId.toString());
        });
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.READY);
    }

    @Test
    void shouldMarkFailedWhenVectorStoreThrows() {
        UUID docId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        var document = pendingDocument(docId, tenantId);
        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));
        doThrow(new RuntimeException("Timeout na OpenAI")).when(vectorStore).write(anyList());

        // o processor NÃO relança — ele captura e marca FAILED (é async, ninguém pega a exceção)
        documentProcessor.process(docId, tenantId,
                "conteúdo".getBytes(), "notas.txt", "fake-s3-key");

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.FAILED);
        assertThat(document.getFailureReason()).contains("Timeout na OpenAI");
    }
}
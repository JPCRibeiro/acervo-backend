package br.app.acervo;

import br.app.acervo.document.domain.DocumentStatus;
import br.app.acervo.document.repository.DocumentRepository;
import br.app.acervo.ingestion.IngestionException;
import br.app.acervo.ingestion.IngestionService;
import br.app.acervo.ingestion.StorageService;
import br.app.acervo.ingestion.FileTypeValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IngestionServiceTest {
    @Mock
    VectorStore vectorStore;

    @Mock
    StorageService storageService;

    @Mock
    DocumentRepository documentRepository;

    @Mock
    FileTypeValidator fileTypeValidator;

    @InjectMocks
    IngestionService ingestionService;

    @Test
    void shouldIngestAndInjectMetadataCorrectly() throws IOException {
        UUID tenantId = UUID.randomUUID();
        UUID fakeId = UUID.randomUUID();

        when(storageService.upload(any(), any(), any())).thenReturn("fake-s3-key");

        when(documentRepository.save(any(br.app.acervo.document.domain.Document.class))).thenAnswer(invocation -> {
            br.app.acervo.document.domain.Document document = invocation.getArgument(0);
            ReflectionTestUtils.setField(document, "id", fakeId);
            return document;
        });

        MockMultipartFile file = new MockMultipartFile(
                "file", "notas.txt", "text/plain",
                "conteúdo de teste para gerar chunks".getBytes());

        ingestionService.ingest(tenantId, file, "notas.txt");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).write(captor.capture());
        List<Document> chunks = captor.getValue();

        assertThat(chunks).isNotEmpty();
        assertThat(chunks).allSatisfy(chunk -> {
            assertThat(chunk.getMetadata()).containsEntry("tenantId", tenantId.toString());
            assertThat(chunk.getMetadata()).containsEntry("source", "notas.txt");
            assertThat(chunk.getMetadata()).containsEntry("s3Key", "fake-s3-key");
            assertThat(chunk.getMetadata()).containsKey("documentId");
        });
    }

    @Test
    void shouldMarkDocumentAsFailedWhenVectorStoreThrowsException() throws IOException {
        UUID tenantId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "teste.pdf", "application/pdf", "dummy".getBytes());

        when(storageService.upload(any(), any(), any())).thenReturn("fake-s3-key");
        when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        doThrow(new RuntimeException("Timeout na OpenAI")).when(vectorStore).write(anyList());

        assertThatThrownBy(() -> ingestionService.ingest(tenantId, file, "teste.pdf"))
                .isInstanceOf(IngestionException.class)
                .hasMessageContaining("Falha ao processar o documento");

        ArgumentCaptor<br.app.acervo.document.domain.Document> docCaptor =
                ArgumentCaptor.forClass(br.app.acervo.document.domain.Document.class);

        verify(documentRepository, times(3)).save(docCaptor.capture());

        var lastSavedState = docCaptor.getValue();
        assertThat(lastSavedState.getStatus()).isEqualTo(DocumentStatus.FAILED);
    }
}
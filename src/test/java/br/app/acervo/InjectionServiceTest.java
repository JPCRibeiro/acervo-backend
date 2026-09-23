package br.app.acervo;

import br.app.acervo.ingestion.IngestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InjectionServiceTest {
    @Mock
    VectorStore vectorStore;

    @InjectMocks
    IngestionService service;

    @Test
    void shouldIngestAndInjectMetadataCorrectly() throws IOException {
        UUID tenantId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file", "notas.txt", "text/plain",
                "conteúdo de teste para gerar chunks".getBytes());

        service.ingest(tenantId, file, "notas.txt");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).write(captor.capture());
        List<Document> chunks = captor.getValue();

        assertThat(chunks).isNotEmpty();
        assertThat(chunks).allSatisfy(chunk -> {
            assertThat(chunk.getMetadata()).containsEntry("tenantId", tenantId.toString());
            assertThat(chunk.getMetadata()).containsEntry("source", "notas.txt");
        });
    }
}

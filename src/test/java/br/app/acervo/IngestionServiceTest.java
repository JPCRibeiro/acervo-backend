package br.app.acervo;

import br.app.acervo.document.repository.DocumentRepository;
import br.app.acervo.ingestion.service.DocumentProcessor;
import br.app.acervo.ingestion.service.FileTypeValidator;
import br.app.acervo.ingestion.service.IngestionService;
import br.app.acervo.ingestion.service.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IngestionServiceTest {

    @Mock
    StorageService storageService;
    @Mock DocumentRepository documentRepository;
    @Mock
    FileTypeValidator fileTypeValidator;
    @Mock
    DocumentProcessor documentProcessor;

    @InjectMocks
    IngestionService ingestionService;

    @Test
    void shouldStoreValidateCreateDocumentAndDispatchProcessing() throws IOException {
        UUID tenantId = UUID.randomUUID();
        UUID fakeId = UUID.randomUUID();

        when(storageService.upload(any(), any(), any())).thenReturn("fake-s3-key");
        when(documentRepository.save(any(br.app.acervo.document.domain.Document.class)))
                .thenAnswer(inv -> {
                    br.app.acervo.document.domain.Document doc = inv.getArgument(0);
                    ReflectionTestUtils.setField(doc, "id", fakeId);
                    return doc;
                });

        MockMultipartFile file = new MockMultipartFile(
                "file", "notas.txt", "text/plain",
                "conteúdo de teste".getBytes());

        UUID resultId = ingestionService.ingest(tenantId, file, "notas.txt");

        verify(fileTypeValidator).validate(file);
        verify(storageService).upload(tenantId, file, "notas.txt");
        verify(documentRepository).save(any(br.app.acervo.document.domain.Document.class));
        verify(documentProcessor).process(eq(fakeId), eq(tenantId), any(byte[].class), eq("notas.txt"), eq("fake-s3-key"));
        assertThat(resultId).isEqualTo(fakeId);
    }
}
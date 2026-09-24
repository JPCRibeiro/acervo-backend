package br.app.acervo.ingestion;

import br.app.acervo.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentProcessor {
    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;
    private final TokenTextSplitter splitter = TokenTextSplitter.builder().build();

    @Async("ingestionExecutor")
    public void process(UUID documentId, UUID tenantId, byte[] fileBytes, String fileName, String s3Key) {
        br.app.acervo.document.domain.Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalStateException("Documento não encontrado na base de dados"));

        try {
            document.startProcessing();
            documentRepository.save(document);

            List<Document> extracted = new TikaDocumentReader(new ByteArrayResource(fileBytes)).read();
            List<Document> chunks = splitter.split(extracted);

            chunks.forEach(chunk -> {
                chunk.getMetadata().put("source", fileName);
                chunk.getMetadata().put("tenantId", tenantId.toString());
                chunk.getMetadata().put("s3Key", s3Key);
                chunk.getMetadata().put("documentId", documentId.toString());
            });

            vectorStore.write(chunks);

            document.completeProcessing(chunks.size());
            documentRepository.save(document);
            log.info("Documento {} processado: {} chunks", documentId, chunks.size());
        } catch (Exception e) {
            log.error("Falha ao processar documento {}", documentId, e);
            document.fail(e.getMessage());
            documentRepository.save(document);
        }
    }
}

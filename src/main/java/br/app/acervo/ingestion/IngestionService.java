package br.app.acervo.ingestion;

import br.app.acervo.document.repository.DocumentRepository;
import org.springframework.ai.document.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngestionService {
    private final VectorStore vectorStore;
    private final StorageService storageService;
    private final DocumentRepository documentRepository;
    private final TokenTextSplitter splitter = TokenTextSplitter.builder().build();
    private final FileTypeValidator fileTypeValidator;

    public IngestionResult ingest(UUID tenantId, MultipartFile file, String fileName) throws IOException {
        fileTypeValidator.validate(file);
        String s3Key = storageService.upload(tenantId, file, fileName);

        br.app.acervo.document.domain.Document document = br.app.acervo.document.domain.Document.create(tenantId, fileName, s3Key);
        documentRepository.save(document);

        document.startProcessing();
        documentRepository.save(document);

        try {
            List<Document> extracted = new TikaDocumentReader(file.getResource()).read();
            List<Document> chunks = splitter.split(extracted);

            chunks.forEach(chunk -> {
                chunk.getMetadata().put("source", fileName);
                chunk.getMetadata().put("tenantId", tenantId.toString());
                chunk.getMetadata().put("s3Key", s3Key);
                chunk.getMetadata().put("documentId", document.getId().toString());
            });

            vectorStore.write(chunks);

            document.completeProcessing(chunks.size());
            documentRepository.save(document);

            return new IngestionResult(document.getId(), s3Key, chunks.size());
        } catch (Exception e) {
            document.fail(e.getMessage());
            documentRepository.save(document);
            throw new IngestionException("Falha ao processar o documento", e);
        }
    }
}
package br.app.acervo.ingestion;

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
    private final TokenTextSplitter splitter = TokenTextSplitter.builder().build();

    public IngestionResult ingest(UUID tenantId, MultipartFile file, String fileName) throws IOException {
        String s3Key = storageService.upload(tenantId, file, fileName);
        List<Document> extracted = new TikaDocumentReader(file.getResource()).read();
        List<Document> chunks = splitter.split(extracted);

        chunks.forEach(chunk -> {
            chunk.getMetadata().put("source", fileName);
            chunk.getMetadata().put("tenantId", tenantId.toString());
            chunk.getMetadata().put("s3Key", s3Key);
        });

        vectorStore.write(chunks);

        return new IngestionResult(s3Key, chunks.size());
    }
}
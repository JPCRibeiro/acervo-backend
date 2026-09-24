package br.app.acervo.ingestion.service;

import br.app.acervo.document.domain.Document;
import br.app.acervo.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngestionService {
    private final StorageService storageService;
    private final DocumentRepository documentRepository;
    private final FileTypeValidator fileTypeValidator;
    private final DocumentProcessor documentProcessor;

    public UUID ingest(UUID organizationId, MultipartFile file, String fileName) throws IOException {
        fileTypeValidator.validate(file);

        String s3Key = storageService.upload(organizationId, file, fileName);

        Document document = Document.create(organizationId, fileName, s3Key);
        documentRepository.save(document);

        byte[] fileBytes = file.getBytes();

        documentProcessor.process(document.getId(), organizationId, fileBytes, fileName, s3Key);

        return document.getId();
    }
}
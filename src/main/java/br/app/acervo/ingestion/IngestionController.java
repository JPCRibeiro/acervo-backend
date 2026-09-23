package br.app.acervo.ingestion;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class IngestionController {
    private final IngestionService service;

    @PostMapping
    public ResponseEntity<IngestionResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tenantId") UUID tenantId) throws IOException {

        String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("unknown");
        IngestionResult result = service.ingest(tenantId, file, fileName);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new IngestionResponse(result.documentId(), fileName, result.chunkCount()));
    }
}

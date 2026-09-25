package br.app.acervo.ingestion.controller;

import br.app.acervo.document.domain.DocumentStatus;
import br.app.acervo.ingestion.dto.IngestionResponse;
import br.app.acervo.ingestion.service.IngestionService;
import br.app.acervo.shared.security.AuthenticatedOrganization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {
        UUID organizationId = AuthenticatedOrganization.id(jwt);
        String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("unknown");
        UUID documentId = service.ingest(organizationId, file, fileName);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new IngestionResponse(documentId, fileName, DocumentStatus.PENDING));
    }
}

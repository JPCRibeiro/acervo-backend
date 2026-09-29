package br.app.acervo.document.controller;

import br.app.acervo.document.dto.DocumentStatusResponse;
import br.app.acervo.document.dto.DocumentSummaryResponse;
import br.app.acervo.document.service.DocumentService;
import br.app.acervo.shared.security.AuthenticatedOrganization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService service;

    @GetMapping("/{id}")
    public ResponseEntity<DocumentStatusResponse> getStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID organizationId = AuthenticatedOrganization.id(jwt);
        return ResponseEntity.ok(service.getDocumentStatus(id, organizationId));
    }

    @GetMapping("/{id}/url")
    public ResponseEntity<Map<String, String>> getUrl(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID organizationId = AuthenticatedOrganization.id(jwt);
        return ResponseEntity.ok(Map.of("url", service.getPresignedUrl(id, organizationId)));
    }

    @GetMapping
    public ResponseEntity<List<DocumentSummaryResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        UUID organizationId = AuthenticatedOrganization.id(jwt);
        return ResponseEntity.ok(service.listByOrganization(organizationId));
    }
}

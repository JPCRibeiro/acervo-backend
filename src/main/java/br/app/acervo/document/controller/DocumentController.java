package br.app.acervo.document.controller;

import br.app.acervo.document.dto.DocumentStatusResponse;
import br.app.acervo.document.repository.DocumentRepository;
import br.app.acervo.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService service;

    @GetMapping("/{id}")
    public ResponseEntity<DocumentStatusResponse> getStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getDocumentStatus(id));
    }
}

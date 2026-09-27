package br.app.acervo.document.service;

import br.app.acervo.document.domain.Document;
import br.app.acervo.document.dto.DocumentStatusResponse;
import br.app.acervo.document.dto.DocumentSummaryResponse;
import br.app.acervo.document.exception.DocumentNotFoundException;
import br.app.acervo.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;

    public DocumentStatusResponse getDocumentStatus(UUID id, UUID organizationId) {
        Document document = documentRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(DocumentNotFoundException::new);

        return new DocumentStatusResponse(document.getId(), document.getFileName(),
                document.getStatus(), document.getChunkCount(), document.getFailureReason());
    }

    @Transactional(readOnly = true)
    public List<DocumentSummaryResponse> listByOrganization(UUID organizationId) {
        return documentRepository.findByOrganizationIdOrderByUploadedAtDesc(organizationId)
                .stream()
                .map(d -> new DocumentSummaryResponse(
                        d.getId(), d.getFileName(), d.getStatus(), d.getChunkCount(),
                        d.getFileSizeBytes(), d.getUploadedAt(), d.getFailureReason()))
                .toList();
    }
}

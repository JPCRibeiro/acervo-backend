package br.app.acervo.document.repository;

import br.app.acervo.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByIdAndOrganizationId(UUID id, UUID organizationId);
}

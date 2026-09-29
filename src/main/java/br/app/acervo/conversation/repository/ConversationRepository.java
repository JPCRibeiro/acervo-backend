package br.app.acervo.conversation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.app.acervo.conversation.domain.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByOrganizationIdAndUserIdOrderByUpdatedAtDesc(UUID organizationId, UUID userId);

    Optional<Conversation> findByIdAndOrganizationIdAndUserId(UUID id, UUID organizationId, UUID userId);
}
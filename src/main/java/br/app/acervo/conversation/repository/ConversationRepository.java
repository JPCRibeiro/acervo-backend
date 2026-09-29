package br.app.acervo.conversation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.app.acervo.conversation.domain.Conversation;

import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
}

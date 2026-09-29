package br.app.acervo.conversation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "conversation_id", nullable = false, updatable = false)
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private MessageRole role;

    @Column(nullable = false, updatable = false, columnDefinition = "text")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<MessageCitation> sources;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private Message(UUID conversationId, MessageRole role, String content, List<MessageCitation> sources) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.sources = sources;
        this.createdAt = Instant.now();
    }

    static Message user(UUID conversationId, String content) {
        validate(conversationId, content);
        return new Message(conversationId, MessageRole.USER, content, null);
    }

    static Message assistant(UUID conversationId, String content, List<MessageCitation> sources) {
        validate(conversationId, content);
        return new Message(conversationId, MessageRole.ASSISTANT, content, sources);
    }

    private static void validate(UUID conversationId, String content) {
        if (conversationId == null) throw new IllegalArgumentException("conversationId é obrigatório");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content é obrigatório");
    }
}
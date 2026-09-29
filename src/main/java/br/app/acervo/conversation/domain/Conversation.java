package br.app.acervo.conversation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversation {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private Conversation(UUID organizationId, UUID userId, String title) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.title = title;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Conversation create(UUID organizationId, UUID userId, String title) {
        if (organizationId == null) throw new IllegalArgumentException("organizationId é obrigatório");
        if (userId == null) throw new IllegalArgumentException("userId é obrigatório");
        return new Conversation(organizationId, userId, normalizeTitle(title));
    }

    public Message addUserMessage(String content) {
        Message message = Message.user(this.id, content);
        touch();
        return message;
    }

    public Message addAssistantMessage(String content, List<MessageCitation> sources) {
        Message message = Message.assistant(this.id, content, sources);
        touch();
        return message;
    }

    public void rename(String title) {
        this.title = normalizeTitle(title);
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title é obrigatório");
        String t = title.strip();
        return t.length() > 200 ? t.substring(0, 200) : t;
    }
}
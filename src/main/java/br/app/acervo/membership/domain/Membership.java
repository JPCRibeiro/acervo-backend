package br.app.acervo.membership.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_organizations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    private Membership(UUID userId, UUID organizationId, Role role) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.role = role;
        this.joinedAt = Instant.now();
    }

    public static Membership create(UUID userId, UUID organizationId, Role role) {
        if (userId == null) {
            throw new IllegalArgumentException("userId é obrigatório");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId é obrigatório");
        }
        if (role == null) {
            throw new IllegalArgumentException("role é obrigatório");
        }
        return new Membership(userId, organizationId, role);
    }

    public void touchLastAccess() {
        this.lastAccessedAt = Instant.now();
    }
}
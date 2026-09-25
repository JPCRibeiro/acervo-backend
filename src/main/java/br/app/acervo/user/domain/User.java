package br.app.acervo.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Column(nullable = false, unique = true, updatable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private User(UUID organizationId, String email, String passwordHash, String name, Role role) {
        this.organizationId = organizationId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.createdAt = Instant.now();
    }

    public static User create(UUID organizationId, String email, String passwordHash, String name, Role role) {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId é obrigatório");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email é obrigatório");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash é obrigatório");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name é obrigatório");
        }
        if (role == null) {
            throw new IllegalArgumentException("role é obrigatório");
        }
        return new User(organizationId, email, passwordHash, name, role);
    }
}
package br.app.acervo.shared.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public final class AuthenticatedUser {
    private AuthenticatedUser() {}

    public static UUID id(Jwt jwt) {
        String userId = jwt.getSubject();
        if (userId == null) {
            throw new IllegalStateException("Token sem subject (userId)");
        }
        return UUID.fromString(userId);
    }
}
package br.app.acervo.shared.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public final class AuthenticatedOrganization {
    private AuthenticatedOrganization() {}

    public static UUID id(Jwt jwt) {
        String organizationId = jwt.getClaimAsString("organizationId");
        if (organizationId == null) {
            throw new IllegalStateException("Token sem claim organizationId");
        }
        return UUID.fromString(organizationId);
    }
}
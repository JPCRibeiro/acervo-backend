package br.app.acervo.organization.dto;

import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name
) {}
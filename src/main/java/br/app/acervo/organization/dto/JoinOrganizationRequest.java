package br.app.acervo.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinOrganizationRequest(
        @NotBlank
        String inviteCode
) {}
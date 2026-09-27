package br.app.acervo.membership.dto;

import br.app.acervo.membership.domain.Role;

import java.util.UUID;

public record OrganizationSummary(UUID id, String name, Role role) {}

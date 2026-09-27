package br.app.acervo.user.dto;

import br.app.acervo.user.domain.Role;

import java.util.UUID;

public record MemberResponse(
        UUID id,
        String name,
        String email,
        Role role
) {}
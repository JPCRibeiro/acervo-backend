package br.app.acervo.membership.dto;

import br.app.acervo.membership.domain.Role;
import java.util.UUID;

public record MemberResponse(UUID id, String name, String email, Role role) {}
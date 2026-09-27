package br.app.acervo.user.dto;

import java.util.UUID;

public record UserProfileResponse(UUID id, String name, String email) {}
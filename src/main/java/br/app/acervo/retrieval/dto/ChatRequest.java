package br.app.acervo.retrieval.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ChatRequest(
        UUID conversationId,
        @NotBlank
        String question
) {}
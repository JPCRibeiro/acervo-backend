package br.app.acervo.retrieval.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank
        String question
) {}

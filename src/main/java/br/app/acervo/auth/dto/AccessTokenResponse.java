package br.app.acervo.auth.dto;

public record AccessTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public static AccessTokenResponse bearer(String accessToken, long expiresIn) {
        return new AccessTokenResponse(accessToken, "Bearer", expiresIn);
    }
}
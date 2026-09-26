package br.app.acervo.auth.service;

import br.app.acervo.auth.domain.RefreshToken;
import br.app.acervo.auth.exception.InvalidRefreshTokenException;
import br.app.acervo.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-ttl}")
    private Duration refreshTokenTtl;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    public record Rotation(UUID userId, String rawToken) {}

    @Transactional
    public String issue(UUID userId) {
        String raw = generateOpaqueToken();
        RefreshToken token = RefreshToken.create(userId, sha256(raw), Instant.now().plus(refreshTokenTtl));
        refreshTokenRepository.save(token);
        return raw;
    }

    @Transactional
    public Rotation rotate(String rawToken) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(sha256(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (stored.getRevokedAt() != null) {
            refreshTokenRepository.revokeAllByUserId(stored.getUserId());
            throw new InvalidRefreshTokenException();
        }
        if (!stored.isActive()) {
            throw new InvalidRefreshTokenException();
        }

        stored.revoke();
        String newRaw = issue(stored.getUserId());
        return new Rotation(stored.getUserId(), newRaw);
    }

    @Transactional
    public void revokeAllByRawToken(String rawToken) {
        refreshTokenRepository.findByTokenHash(sha256(rawToken))
                .ifPresent(t -> refreshTokenRepository.revokeAllByUserId(t.getUserId()));
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar hash do token", e);
        }
    }
}
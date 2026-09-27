package br.app.acervo.auth.controller;

import br.app.acervo.auth.dto.*;
import br.app.acervo.auth.exception.InvalidRefreshTokenException;
import br.app.acervo.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_COOKIE = "refreshToken";
    private final AuthService service;

    @Value("${jwt.refresh-token-ttl}")
    private Duration refreshTtl;

    @PostMapping("/register")
    public ResponseEntity<AccessTokenResponse> register(@Valid @RequestBody RegisterRequest req) {
        return authResponse(service.register(req), HttpStatus.CREATED);
    }

    @PostMapping("/join")
    public ResponseEntity<AccessTokenResponse> join(@Valid @RequestBody JoinRequest req) {
        return authResponse(service.join(req), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return authResponse(service.login(req), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            @RequestBody(required = false) RefreshRequest body) {
        if (refreshToken == null) throw new InvalidRefreshTokenException();
        UUID requestedOrg = (body != null) ? body.organizationId() : null;
        return authResponse(service.refresh(refreshToken, requestedOrg), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        if (refreshToken != null) service.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookie("", Duration.ZERO).toString())
                .build();
    }

    private ResponseEntity<AccessTokenResponse> authResponse(
            AuthService.AuthResult result,
            HttpStatus status
    ) {
        ResponseCookie cookie = refreshCookie(result.refreshToken(), refreshTtl);
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(AccessTokenResponse.bearer(result.accessToken(), result.expiresIn()));
    }

    private ResponseCookie refreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }
}
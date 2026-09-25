package br.app.acervo.auth.controller;

import br.app.acervo.auth.dto.AccessTokenResponse;
import br.app.acervo.auth.dto.JoinRequest;
import br.app.acervo.auth.dto.LoginRequest;
import br.app.acervo.auth.dto.RegisterRequest;
import br.app.acervo.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<AccessTokenResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(req));
    }

    @PostMapping("/join")
    public ResponseEntity<AccessTokenResponse> join(@Valid @RequestBody JoinRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.join(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.status(HttpStatus.OK).body(service.login(req));
    }
}

package br.app.acervo.membership.controller;

import br.app.acervo.membership.dto.MemberResponse;
import br.app.acervo.membership.service.MembershipService;
import br.app.acervo.shared.security.AuthenticatedOrganization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MembershipController {
    private final MembershipService service;

    @GetMapping
    public ResponseEntity<List<MemberResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        UUID organizationId = AuthenticatedOrganization.id(jwt);
        return ResponseEntity.ok(service.listMembers(organizationId));
    }
}
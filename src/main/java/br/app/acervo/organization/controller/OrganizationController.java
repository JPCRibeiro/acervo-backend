package br.app.acervo.organization.controller;

import br.app.acervo.organization.dto.InviteCodeResponse;
import br.app.acervo.organization.dto.OrganizationResponse;
import br.app.acervo.organization.service.OrganizationService;
import br.app.acervo.shared.security.AuthenticatedOrganization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService service;

    @GetMapping("/me")
    public ResponseEntity<OrganizationResponse> getMyOrganization(@AuthenticationPrincipal Jwt jwt) {
        UUID organizationId = AuthenticatedOrganization.id(jwt);
        return ResponseEntity.ok(service.getById(organizationId));
    }

    @GetMapping("/invite-code")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<InviteCodeResponse> getInviteCode(@AuthenticationPrincipal Jwt jwt) {
        UUID organizationId = AuthenticatedOrganization.id(jwt);
        return ResponseEntity.ok(service.getInviteCode(organizationId));
    }
}
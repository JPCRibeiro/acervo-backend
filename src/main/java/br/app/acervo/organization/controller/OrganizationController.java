package br.app.acervo.organization.controller;

import br.app.acervo.membership.dto.OrganizationSummary;
import br.app.acervo.membership.service.MembershipService;
import br.app.acervo.organization.dto.CreateOrganizationRequest;
import br.app.acervo.organization.dto.InviteCodeResponse;
import br.app.acervo.organization.dto.JoinOrganizationRequest;
import br.app.acervo.organization.dto.OrganizationResponse;
import br.app.acervo.organization.service.OrganizationService;
import br.app.acervo.shared.security.AuthenticatedOrganization;
import br.app.acervo.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService service;
    private final MembershipService membershipService;

    @GetMapping
    public ResponseEntity<List<OrganizationSummary>> listMine(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = AuthenticatedUser.id(jwt);
        return ResponseEntity.ok(membershipService.listOrganizations(userId));
    }

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

    @PostMapping
    public ResponseEntity<OrganizationSummary> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateOrganizationRequest req) {
        UUID userId = AuthenticatedUser.id(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createForUser(userId, req.name()));
    }

    @PostMapping("/join")
    public ResponseEntity<OrganizationSummary> join(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody JoinOrganizationRequest req) {
        UUID userId = AuthenticatedUser.id(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.joinByInviteCode(userId, req.inviteCode()));
    }
}
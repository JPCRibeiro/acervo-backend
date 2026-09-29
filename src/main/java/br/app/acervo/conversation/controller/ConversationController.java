package br.app.acervo.conversation.controller;

import br.app.acervo.conversation.dto.ConversationDetailResponse;
import br.app.acervo.conversation.dto.ConversationSummaryResponse;
import br.app.acervo.conversation.service.ConversationService;
import br.app.acervo.shared.security.AuthenticatedOrganization;
import br.app.acervo.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService service;

    @GetMapping
    public List<ConversationSummaryResponse> list(@AuthenticationPrincipal Jwt jwt) {
        UUID organizationId = AuthenticatedOrganization.id(jwt);
        UUID userId = AuthenticatedUser.id(jwt);
        return service.list(organizationId, userId);
    }

    @GetMapping("/{id}")
    public ConversationDetailResponse get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID organizationId = AuthenticatedOrganization.id(jwt);
        UUID userId = AuthenticatedUser.id(jwt);
        return service.get(id, organizationId, userId);
    }
}
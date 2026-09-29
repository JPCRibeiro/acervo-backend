package br.app.acervo.retrieval.controller;

import br.app.acervo.retrieval.dto.ChatRequest;
import br.app.acervo.retrieval.dto.ChatStreamResponse;
import br.app.acervo.retrieval.service.ChatService;
import br.app.acervo.shared.security.AuthenticatedOrganization;
import br.app.acervo.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class RetrievalController {
    private final ChatService chatService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatStreamResponse> askStream(
            @Valid @RequestBody ChatRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID organizationId = AuthenticatedOrganization.id(jwt);
        UUID userId = AuthenticatedUser.id(jwt);
        return chatService.streamAndPersist(organizationId, userId, req.conversationId(), req.question());
    }
}
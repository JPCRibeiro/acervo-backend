package br.app.acervo.retrieval.controller;

import br.app.acervo.retrieval.dto.ChatRequest;
import br.app.acervo.retrieval.dto.ChatResponse;
import br.app.acervo.retrieval.service.RetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class RetrievalController {
    private final RetrievalService service;

    @PostMapping
    public ResponseEntity<ChatResponse> ask(@RequestBody ChatRequest req) {
        String answer = service.ask(req.tenantId(), req.question());
        return ResponseEntity.ok(new ChatResponse(answer));
    }
}
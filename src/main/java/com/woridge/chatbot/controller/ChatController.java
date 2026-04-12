package com.woridge.chatbot.controller;

import com.woridge.chatbot.dto.SuccessResponse;
import com.woridge.chatbot.dto.request.ChatRequest;
import com.woridge.chatbot.dto.response.ChatResponse;
import com.woridge.chatbot.exception.chat
        .TenantMissingException;
import com.woridge.chatbot.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<SuccessResponse<ChatResponse>>
    chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(
                    value  = "X-Subdomain",
                    required = false
            ) String subdomain,
            @RequestHeader(
                    value  = "Authorization",
                    required = false
            ) String authorization) {

        // ── Tenant depuis Kong X-Subdomain ──
        // Kong injecte automatiquement ce header
        // depuis le plugin tenant (handler.lua)
        // localhost → "dev-local"
        // atlas.woridge.com → "atlas"
        if (subdomain != null
                && !subdomain.isBlank()) {
            request.setTenant(subdomain);
        }

        // ── Validation tenant ──
        if (request.getTenant() == null
                || request.getTenant().isBlank()) {
            throw new TenantMissingException(
                    "Tenant non identifié — "
                            + "X-Subdomain header manquant"
            );
        }

        log.info(
                "Chat — tenant: {} user: {} role: {}",
                request.getTenant(),
                request.getUserId(),
                request.getRole()
        );

        ChatResponse response =
                chatService.chat(request);

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        response,
                        "Réponse générée avec succès"
                )
        );
    }

    @GetMapping("/health")
    public ResponseEntity<SuccessResponse<String>>
    health() {
        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        "OK",
                        "ms-chatbot opérationnel"
                )
        );
    }
}
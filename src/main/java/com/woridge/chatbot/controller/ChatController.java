package com.woridge.chatbot.controller;

import com.woridge.chatbot.dto.request.ChatRequest;
import com.woridge.chatbot.dto.response.ChatResponse;
import com.woridge.chatbot.dto.SuccessResponse;
import com.woridge.chatbot.service.ChatService;
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
    public ResponseEntity<SuccessResponse<ChatResponse>> chat(
            @RequestBody ChatRequest request,
            @RequestHeader(value = "X-User-CIN", required = false) String userCinHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        log.info("Chat — user:{} role:{}", userCinHeader, roleHeader);

        if (userCinHeader != null && !userCinHeader.isBlank()) {
            request.setUserId(userCinHeader);
        }

        if (roleHeader != null && !roleHeader.isBlank()) {
            request.setRole(roleHeader);
        }

        ChatResponse response = chatService.chat(request);

        return ResponseEntity.ok(
                SuccessResponse.createSuccessResponse(
                        response,
                        "Réponse générée avec succès"
                )
        );
    }
}
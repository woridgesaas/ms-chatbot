package com.woridge.chatbot.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ChatRequest {

    @NotBlank(message = "Question requise")
    private String question;

    // ← Injectés par Kong via headers
    // X-User-CIN, X-User-Role
    private String userId;
    private String role;
}
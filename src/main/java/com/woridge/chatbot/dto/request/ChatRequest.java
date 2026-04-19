package com.woridge.chatbot.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ChatRequest {

    @NotBlank(message = "Question requise")
    private String question;

    // ← plus obligatoires
    // Injectés par Kong via headers
    private String userId;
    private String role;
    private String tenant;
}
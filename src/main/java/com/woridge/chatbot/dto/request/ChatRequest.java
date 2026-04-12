package com.woridge.chatbot.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "La question ne peut pas être vide")
    private String question;

    @NotBlank(message = "L'identifiant utilisateur est requis")
    private String userId;

    @NotBlank(message = "Le rôle est requis")
    private String role;

    @NotBlank(message = "Le tenant est requis")
    private String tenant;
}
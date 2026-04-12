package com.woridge.chatbot.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserChatbotSyncEvent {
    private String userId;
    private String nom;
    private String prenom;
    private String email;
    private String departement;
    private String poste;
    private String role;
    private String tenant;
}
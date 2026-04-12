package com.woridge.chatbot.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserUpdatedEvent {
    private String userId;
    private String nom;
    private String prenom;
    private String email;
    private String departement;
    private String poste;
    private String tenant;
}
package com.woridge.chatbot.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CongeTermineEvent {
    private String employeId;
    private int nombreJours;
    private int soldeRestant;
    private String tenant;
}
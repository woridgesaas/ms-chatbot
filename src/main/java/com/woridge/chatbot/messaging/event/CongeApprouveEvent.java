package com.woridge.chatbot.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CongeApprouveEvent {
    private String employeId;
    private String typeConge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int nombreJours;
    private int soldeRestant;
    private String tenant;
}
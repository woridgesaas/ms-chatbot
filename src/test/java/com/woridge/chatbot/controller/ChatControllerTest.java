package com.woridge.chatbot.controller;

import com.fasterxml.jackson.databind
        .ObjectMapper;
import com.woridge.chatbot.dto.request
        .ChatRequest;
import com.woridge.chatbot.dto.response
        .ChatResponse;
import com.woridge.chatbot.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory
        .annotation.Autowired;
import org.springframework.boot.test.autoconfigure
        .web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito
        .MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet
        .MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet
        .request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet
        .result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @Test
    @DisplayName(
            "POST /api/chat retourne réponse OK"
    )
    void chat_validRequest_returnsOk()
            throws Exception {

        // ── Given ──
        ChatRequest request = new ChatRequest();
        request.setQuestion("mon solde");

        ChatResponse mockResponse =
                ChatResponse.builder()
                        .answer(
                                "18 jours restants"
                        )
                        .intent("SOLDE_CONGE")
                        .confidence(0.98)
                        .build();

        when(chatService.chat(any(
                ChatRequest.class
        ))).thenReturn(mockResponse);

        // ── When & Then ──
        mockMvc.perform(post("/api/chat")
                        .contentType(
                                MediaType
                                        .APPLICATION_JSON
                        )
                        .header(
                                "X-User-CIN",
                                "S760251"
                        )
                        .header(
                                "X-User-Role",
                                "EMPLOYEE"
                        )
                        .content(objectMapper
                                .writeValueAsString(
                                        request
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.data.answer"
                ).value(
                        "18 jours restants"
                ))
                .andExpect(jsonPath(
                        "$.data.intent"
                ).value("SOLDE_CONGE"))
                .andExpect(jsonPath(
                        "$.data.confidence"
                ).value(0.98));
    }

    @Test
    @DisplayName(
            "Kong headers écrasent le body"
    )
    void chat_kongHeaders_overrideBody()
            throws Exception {

        ChatRequest request = new ChatRequest();
        request.setQuestion("mon solde");
        request.setUserId("HACKER");
        request.setRole("ADMIN");

        ChatResponse mockResponse =
                ChatResponse.builder()
                        .answer("réponse")
                        .intent("SOLDE_CONGE")
                        .confidence(0.9)
                        .build();

        when(chatService.chat(any()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/chat")
                        .contentType(
                                MediaType
                                        .APPLICATION_JSON
                        )
                        .header(
                                "X-User-CIN",
                                "S760251"
                        )
                        .header(
                                "X-User-Role",
                                "EMPLOYEE"
                        )
                        .content(objectMapper
                                .writeValueAsString(
                                        request
                                ))
                )
                .andExpect(status().isOk());

        // ChatService reçoit les valeurs
        // de Kong (pas du body)
        verify(chatService).chat(argThat(
                req ->
                        req.getUserId()
                                .equals("S760251")
                                && req.getRole()
                                .equals("EMPLOYEE")
        ));
    }

    @Test
    @DisplayName(
            "Sans X-User-CIN → userId body gardé"
    )
    void chat_noUserCin_keepsBodyUserId()
            throws Exception {

        ChatRequest request = new ChatRequest();
        request.setQuestion("mon solde");
        request.setUserId("S111111");
        request.setRole("EMPLOYEE");

        ChatResponse mockResponse =
                ChatResponse.builder()
                        .answer("réponse")
                        .intent("SOLDE_CONGE")
                        .confidence(0.9)
                        .build();

        when(chatService.chat(any()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/chat")
                        .contentType(
                                MediaType
                                        .APPLICATION_JSON
                        )
                        .content(objectMapper
                                .writeValueAsString(
                                        request
                                ))
                )
                .andExpect(status().isOk());

        // userId du body reste S111111
        verify(chatService).chat(argThat(
                req -> req.getUserId()
                        .equals("S111111")
        ));
    }
}
package com.woridge.chatbot.service;

import com.woridge.chatbot.config.WoridgeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function
        .client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final WebClient groqWebClient;
    private final WoridgeProperties properties;

    public String generateResponse(
            String question,
            String data,
            List<String> ragChunks) {

        String ragContext = ragChunks.isEmpty()
                ? ""
                : "\n\nContexte reglementation:\n"
                + String.join("\n---\n", ragChunks);

        String prompt = String.format("""
    Tu es l'assistant RH de Woridge.
    Reponds en francais en 2-3 phrases maximum.
    Sois direct et concis.
    Utilise uniquement les donnees fournies.
    Ne fais pas de calculs non demandes.
    
    Donnees RH:
    %s
    %s
    
    Question: %s
    
    Reponse courte:
    """,
                data, ragContext, question
        );

        try {
            Map<String, Object> body = Map.of(
                    "model",
                    properties.getGroq().getModel(),
                    "messages", List.of(
                            Map.of(
                                    "role",    "user",
                                    "content", prompt
                            )
                    ),
                    "max_tokens",
                    properties.getGroq().getMaxTokens()
            );

            Map response = groqWebClient
                    .post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            List choices =
                    (List) response.get("choices");
            Map choice  = (Map) choices.get(0);
            Map message = (Map) choice.get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("Groq erreur : {}",
                    e.getMessage());
            return "Je suis desole, je ne peux "
                    + "pas repondre pour le moment.";
        }
    }
}
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

        // ── Détecter la langue ──
        String lang = detectLanguage(question);
        String langInstruction;

        switch (lang) {
            case "arabic" ->
                    langInstruction =
                            "You MUST respond entirely "
                                    + "in Arabic language. "
                                    + "كل الإجابة يجب أن تكون "
                                    + "باللغة العربية فقط.";
            case "english" ->
                    langInstruction =
                            "You MUST respond entirely "
                                    + "in English language. "
                                    + "Do not use French or Arabic.";
            default ->
                    langInstruction =
                            "Tu DOIS repondre "
                                    + "entierement en francais.";
        }

        String prompt = String.format("""
            Tu es l'assistant RH de Woridge.
            LANGUE : %s
            Reponds en 2-3 phrases maximum.
            Sois direct et concis.
            Utilise uniquement les donnees
            fournies ci-dessous.
            Ne fais pas de calculs non demandes.

            Donnees RH:
            %s
            %s

            Question: %s

            Reponse:
            """,
                langInstruction,
                data, ragContext, question
        );

        // ── Essayer Ollama local d'abord ──
        if (properties.getOllama().isEnabled()) {
            try {
                String result =
                        generateWithOllama(prompt);
                if (result != null
                        && !result.isBlank()) {
                    log.info(
                            "Reponse via Ollama ✅"
                    );
                    return result.trim();
                }
            } catch (Exception e) {
                log.warn(
                        "Ollama timeout → fallback "
                                + "Groq : {}",
                        e.getMessage()
                );
            }
        }

        // ── Fallback Groq ──
        return generateWithGroq(prompt);
    }

    // ── Détecter la langue ──
    private String detectLanguage(
            String question) {

        if (question == null
                || question.isBlank()) {
            return "french";
        }

        // Arabe — détecter par les caractères
        if (question.matches(
                ".*[\\u0600-\\u06FF].*")) {
            return "arabic";
        }

        // Anglais — détecter par mots clés
        String lower = question.toLowerCase();
        String[] englishWords = {
                " my ", " how ", " what ", " when ",
                " where ", " who ", " is ", " are ",
                " the ", " leave ", " balance ",
                " salary ", " employee ", " many ",
                " today ", " this ", " show ",
                " list ", " total ", " absent ",
                " days ", " marriage ", " payslip "
        };
        for (String word : englishWords) {
            if (lower.contains(word)
                    || lower.startsWith(
                    word.trim()
            )) {
                return "english";
            }
        }

        return "french";
    }

    // ── Génération Ollama local ──
    private String generateWithOllama(
            String prompt) {

        log.info(
                "Appel Ollama : {}",
                properties.getOllama().getModel()
        );

        Map<String, Object> body = Map.of(
                "model",
                properties.getOllama().getModel(),
                "messages", List.of(
                        Map.of(
                                "role",    "user",
                                "content", prompt
                        )
                ),
                "max_tokens",
                properties.getGroq().getMaxTokens(),
                "stream", false
        );

        Map response = WebClient.builder()
                .baseUrl(
                        properties.getOllama().getUrl()
                                + "/v1"
                )
                .build()
                .post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        if (response == null) return null;

        List choices =
                (List) response.get("choices");
        if (choices == null
                || choices.isEmpty()) {
            return null;
        }

        Map choice  = (Map) choices.get(0);
        Map message = (Map) choice.get("message");
        return (String) message.get("content");
    }

    // ── Fallback Groq API ──
    private String generateWithGroq(
            String prompt) {

        log.info("Appel Groq API (fallback)");

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

            if (response == null) {
                return "Je suis desole, "
                        + "reessayez plus tard.";
            }

            List choices =
                    (List) response.get("choices");
            Map choice  = (Map) choices.get(0);
            Map message =
                    (Map) choice.get("message");

            return ((String) message
                    .get("content")).trim();

        } catch (Exception e) {
            log.error(
                    "Groq erreur : {}",
                    e.getMessage()
            );
            return "Je suis desole, "
                    + "je ne peux pas repondre "
                    + "pour le moment.";
        }
    }
}
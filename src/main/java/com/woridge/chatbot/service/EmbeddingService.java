package com.woridge.chatbot.service;

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
public class EmbeddingService {

    private final WebClient classifierWebClient;

    /**
     * Génère un vecteur de 384 dimensions
     * via ms-classifier /embed
     * sentence-transformers multilingue
     */
    public float[] embed(String text) {
        try {
            Map response = classifierWebClient
                    .post()
                    .uri("/embed")
                    .bodyValue(Map.of("text", text))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null) return null;

            List<Double> vector =
                    (List<Double>) response
                            .get("vector");

            if (vector == null) return null;

            float[] result =
                    new float[vector.size()];
            for (int i = 0;
                 i < vector.size(); i++) {
                result[i] =
                        vector.get(i).floatValue();
            }

            log.debug(
                    "Embedding généré — {} dim",
                    result.length
            );
            return result;

        } catch (Exception e) {
            log.error(
                    "Erreur embedding : {}",
                    e.getMessage()
            );
            return null;
        }
    }

    /**
     * Convertit float[] en format PostgreSQL
     * [0.1, 0.2, ...] → "[0.1,0.2,...]"
     */
    public String toPostgresVector(
            float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
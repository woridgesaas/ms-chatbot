package com.woridge.chatbot.config;

import lombok.Data;
import org.springframework.boot.context.properties
        .ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "woridge")
public class WoridgeProperties {

    private Classifier classifier =
            new Classifier();
    private Groq groq = new Groq();
    private Ollama ollama = new Ollama();
    private Rag rag = new Rag();

    @Data
    public static class Classifier {
        private String url;
    }

    @Data
    public static class Groq {
        private String apiKey;
        private String apiUrl;
        private String model;
        private String embeddingModel;
        private int maxTokens;
    }

    @Data
    public static class Ollama {
        // URL du serveur Ollama
        private String url =
                "http://localhost:11434";
        // Modèle à utiliser
        private String model = "mistral";
        // Activer/désactiver Ollama
        // false → utilise Groq
        private boolean enabled = true;
    }

    @Data
    public static class Rag {
        private int chunkSize;
        private int topK;
        private double confidenceThreshold;
    }
}
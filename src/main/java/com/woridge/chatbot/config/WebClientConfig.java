package com.woridge.chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // WebClient pour appeler ms-classifier Python
    @Bean("classifierWebClient")
    public WebClient classifierWebClient(WoridgeProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.getClassifier().getUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // WebClient pour appeler Groq API
    @Bean("groqWebClient")
    public WebClient groqWebClient(WoridgeProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.getGroq().getApiUrl())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader(
                        "Authorization",
                        "Bearer " + properties.getGroq().getApiKey()
                )
                .build();
    }
}
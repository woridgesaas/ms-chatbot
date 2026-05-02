package com.woridge.chatbot.service;

import com.woridge.chatbot.dto.response.IntentResult;
import com.woridge.chatbot.enumeration.IntentType;
import com.woridge.chatbot.exception.classifier
        .ClassifierUnavailableException;
import com.woridge.chatbot.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client
        .WebClient;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassifierService {

    private final WebClient classifierWebClient;
    private final TextUtil textUtil;

    @Cacheable(
            value = "intent-cache",
            key = "@textUtil.normalize(#question)"
    )
    public IntentResult classify(String question) {
        log.info("Classification : {}", question);

        try {
            IntentResult result = classifierWebClient
                    .post()
                    .uri("/classify")
                    .bodyValue(Map.of("question", question))
                    .retrieve()
                    .bodyToMono(IntentResult.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            log.info("Intent : {} confidence : {}",
                    result.getIntent(),
                    result.getConfidence());

            return result;

        } catch (Exception e) {
            log.error("ms-classifier indisponible : {}",
                    e.getMessage());
            throw new ClassifierUnavailableException(
                    "ms-classifier indisponible : "
                            + e.getMessage()
            );
        }
    }
}
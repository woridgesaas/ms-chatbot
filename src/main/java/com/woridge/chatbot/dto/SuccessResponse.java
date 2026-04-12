package com.woridge.chatbot.dto;

import java.time.Instant;

public record SuccessResponse<T>(
        Instant timestamp,
        T data,
        String message
) {
    public static <T> SuccessResponse<T> createSuccessResponse(
            T data,
            String message) {
        return new SuccessResponse<>(
                Instant.now(),
                data,
                message
        );
    }
}
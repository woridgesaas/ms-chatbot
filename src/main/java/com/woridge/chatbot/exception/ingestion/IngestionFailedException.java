
package com.woridge.chatbot.exception.ingestion;

import com.woridge.chatbot.exception.ChatbotException;

public class IngestionFailedException
        extends ChatbotException {
    public IngestionFailedException(String message) {
        super(message);
    }
}
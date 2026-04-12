
package com.woridge.chatbot.exception.ingestion;

import com.woridge.chatbot.exception.ChatbotException;

public class FileEmptyException
        extends ChatbotException {
    public FileEmptyException(String message) {
        super(message);
    }
}
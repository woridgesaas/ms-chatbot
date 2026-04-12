// exception/chat/AccessDeniedException.java
package com.woridge.chatbot.exception.chat;

import com.woridge.chatbot.exception.ChatbotException;

public class AccessDeniedException
        extends ChatbotException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
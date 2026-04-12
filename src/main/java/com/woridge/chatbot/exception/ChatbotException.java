// exception/ChatbotException.java
// Exception générique de base
package com.woridge.chatbot.exception;

public class ChatbotException extends RuntimeException {
    public ChatbotException(String message) {
        super(message);
    }
}
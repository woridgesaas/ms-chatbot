
package com.woridge.chatbot.exception.classifier;

import com.woridge.chatbot.exception.ChatbotException;

public class ClassifierUnavailableException
        extends ChatbotException {
    public ClassifierUnavailableException(String message) {
        super(message);
    }
}
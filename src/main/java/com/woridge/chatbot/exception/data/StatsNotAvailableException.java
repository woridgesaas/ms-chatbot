
package com.woridge.chatbot.exception.data;

import com.woridge.chatbot.exception.ChatbotException;

public class StatsNotAvailableException
        extends ChatbotException {
    public StatsNotAvailableException(String message) {
        super(message);
    }
}
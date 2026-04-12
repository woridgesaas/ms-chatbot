
package com.woridge.chatbot.exception.chat;

import com.woridge.chatbot.exception.ChatbotException;

public class TenantMissingException
        extends ChatbotException {
    public TenantMissingException(String message) {
        super(message);
    }
}
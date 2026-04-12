
package com.woridge.chatbot.exception.data;

import com.woridge.chatbot.exception.ChatbotException;

public class EmployeeNotFoundException
        extends ChatbotException {
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
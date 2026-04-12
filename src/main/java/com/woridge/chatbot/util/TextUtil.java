package com.woridge.chatbot.util;

import org.springframework.stereotype.Component;

@Component("textUtil")
public class TextUtil {

    public String normalize(String text) {
        if (text == null) return "";
        return text
                .toLowerCase()
                .trim()
                .replaceAll("[?!.,;:']", "")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[àâä]", "a")
                .replaceAll("[ùûü]", "u")
                .replaceAll("[îï]", "i")
                .replaceAll("[ôö]", "o")
                .replaceAll("[ç]", "c")
                .replaceAll("\\s+", " ");
    }
}
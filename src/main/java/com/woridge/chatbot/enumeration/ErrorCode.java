package com.woridge.chatbot.enumeration;

public enum ErrorCode {

    // ── Général ──
    INTERNAL_ERROR,
    VALIDATION_ERROR,
    RESOURCE_NOT_FOUND,

    // ── Chat ──
    QUESTION_EMPTY,
    INTENT_NOT_FOUND,
    ACCESS_DENIED,
    TENANT_MISSING,

    // ── Classifier ──
    CLASSIFIER_UNAVAILABLE,
    CLASSIFIER_TIMEOUT,

    // ── LLM / Groq ──
    LLM_UNAVAILABLE,
    LLM_TIMEOUT,
    EMBEDDING_FAILED,

    // ── RAG / Ingestion ──
    INGESTION_FAILED,
    DOCUMENT_TYPE_INVALID,
    FILE_EMPTY,

    // ── Données ──
    EMPLOYEE_NOT_FOUND,
    STATS_NOT_AVAILABLE
}
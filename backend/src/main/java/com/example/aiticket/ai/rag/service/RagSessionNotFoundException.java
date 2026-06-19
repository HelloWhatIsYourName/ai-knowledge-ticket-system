package com.example.aiticket.ai.rag.service;

public class RagSessionNotFoundException extends RuntimeException {
    public RagSessionNotFoundException() {
        super("AI session not found");
    }
}

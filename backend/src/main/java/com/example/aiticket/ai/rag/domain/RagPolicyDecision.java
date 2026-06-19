package com.example.aiticket.ai.rag.domain;

public record RagPolicyDecision(
        boolean canAnswer,
        double confidence,
        boolean transferSuggested,
        String transferReason
) {
}

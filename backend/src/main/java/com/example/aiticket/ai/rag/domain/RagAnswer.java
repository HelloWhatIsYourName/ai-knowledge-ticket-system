package com.example.aiticket.ai.rag.domain;

import java.util.List;

public record RagAnswer(
        Long sessionId,
        Long userMessageId,
        Long assistantMessageId,
        String answer,
        boolean canAnswer,
        double confidence,
        boolean transferSuggested,
        String transferReason,
        List<RagCitation> citations
) {
}

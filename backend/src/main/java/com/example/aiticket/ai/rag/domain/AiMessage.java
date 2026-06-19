package com.example.aiticket.ai.rag.domain;

import java.time.LocalDateTime;

public record AiMessage(
        Long id,
        Long sessionId,
        Long userId,
        AiMessageRole role,
        String content,
        String modelName,
        Boolean canAnswer,
        Double confidence,
        Boolean transferSuggested,
        String transferReason,
        LocalDateTime createdAt
) {
}

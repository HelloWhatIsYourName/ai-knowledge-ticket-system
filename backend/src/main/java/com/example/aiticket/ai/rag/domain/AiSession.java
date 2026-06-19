package com.example.aiticket.ai.rag.domain;

import java.time.LocalDateTime;

public record AiSession(
        Long id,
        Long userId,
        String title,
        String lastQuestion,
        Boolean transferSuggested,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

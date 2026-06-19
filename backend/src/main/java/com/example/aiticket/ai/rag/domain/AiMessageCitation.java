package com.example.aiticket.ai.rag.domain;

import java.time.LocalDateTime;

public record AiMessageCitation(
        Long id,
        Long messageId,
        Long chunkId,
        Long documentId,
        Integer citationIndex,
        String sourceTitle,
        String snippet,
        Double similarity,
        LocalDateTime createdAt
) {
}

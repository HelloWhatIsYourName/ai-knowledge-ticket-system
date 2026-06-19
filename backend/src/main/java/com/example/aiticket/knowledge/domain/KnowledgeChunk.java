package com.example.aiticket.knowledge.domain;

import java.time.LocalDateTime;

public record KnowledgeChunk(
        Long id,
        Long documentId,
        Long categoryId,
        Integer chunkIndex,
        String content,
        String contentHash,
        String sourceTitle,
        Integer sourcePage,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

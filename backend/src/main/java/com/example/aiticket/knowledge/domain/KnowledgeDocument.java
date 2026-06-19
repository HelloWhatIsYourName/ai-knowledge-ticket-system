package com.example.aiticket.knowledge.domain;

import java.time.LocalDateTime;

public record KnowledgeDocument(
        Long id,
        String title,
        Long categoryId,
        String fileName,
        String storageName,
        String fileType,
        Long fileSize,
        Boolean enabled,
        KnowledgeParseStatus parseStatus,
        String parseError,
        Integer retryCount,
        Long uploadedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean deleted
) {
}

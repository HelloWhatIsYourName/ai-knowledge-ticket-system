package com.example.aiticket.knowledge.web;

import com.example.aiticket.knowledge.domain.KnowledgeDocument;

public record DocumentResponse(
        Long id,
        String title,
        Long categoryId,
        Boolean enabled,
        String parseStatus,
        String parseError,
        Integer retryCount
) {
    public static DocumentResponse from(KnowledgeDocument document) {
        return new DocumentResponse(
                document.id(),
                document.title(),
                document.categoryId(),
                document.enabled(),
                document.parseStatus().name(),
                document.parseError(),
                document.retryCount()
        );
    }
}

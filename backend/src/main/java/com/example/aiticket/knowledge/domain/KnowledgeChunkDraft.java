package com.example.aiticket.knowledge.domain;

public record KnowledgeChunkDraft(
        Long documentId,
        Long categoryId,
        Integer chunkIndex,
        String content,
        String contentHash,
        String sourceTitle,
        Integer sourcePage,
        String vectorLiteral
) {
}

package com.example.aiticket.knowledge.domain;

public record KnowledgeSearchResult(
        Long chunkId,
        Long documentId,
        Long categoryId,
        Integer chunkIndex,
        String content,
        String sourceTitle,
        Double distance,
        Double similarity
) {
}

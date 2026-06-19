package com.example.aiticket.ai.rag.domain;

public record RagCitation(
        Integer citationIndex,
        Long chunkId,
        Long documentId,
        Long categoryId,
        String sourceTitle,
        Double similarity,
        String snippet
) {
}

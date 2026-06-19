package com.example.aiticket.knowledge.web;

import com.example.aiticket.knowledge.domain.KnowledgeSearchResult;

public record SearchKnowledgeResponse(
        Long chunkId,
        Long documentId,
        Long categoryId,
        Integer chunkIndex,
        String content,
        String sourceTitle,
        Double distance,
        Double similarity
) {
    public static SearchKnowledgeResponse from(KnowledgeSearchResult result) {
        return new SearchKnowledgeResponse(
                result.chunkId(),
                result.documentId(),
                result.categoryId(),
                result.chunkIndex(),
                result.content(),
                result.sourceTitle(),
                result.distance(),
                result.similarity()
        );
    }
}

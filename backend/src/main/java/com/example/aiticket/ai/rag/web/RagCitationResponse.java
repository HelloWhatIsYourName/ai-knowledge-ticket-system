package com.example.aiticket.ai.rag.web;

import com.example.aiticket.ai.rag.domain.AiMessageCitation;
import com.example.aiticket.ai.rag.domain.RagCitation;

public record RagCitationResponse(
        Integer citationIndex,
        Long chunkId,
        Long documentId,
        Long categoryId,
        String sourceTitle,
        Double similarity,
        String snippet
) {
    public static RagCitationResponse from(RagCitation citation) {
        return new RagCitationResponse(
                citation.citationIndex(),
                citation.chunkId(),
                citation.documentId(),
                citation.categoryId(),
                citation.sourceTitle(),
                citation.similarity(),
                citation.snippet()
        );
    }

    public static RagCitationResponse from(AiMessageCitation citation) {
        return new RagCitationResponse(
                citation.citationIndex(),
                citation.chunkId(),
                citation.documentId(),
                null,
                citation.sourceTitle(),
                citation.similarity(),
                citation.snippet()
        );
    }
}

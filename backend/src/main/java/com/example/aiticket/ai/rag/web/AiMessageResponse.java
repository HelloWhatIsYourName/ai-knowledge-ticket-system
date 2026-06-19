package com.example.aiticket.ai.rag.web;

import com.example.aiticket.ai.rag.domain.AiMessageWithCitations;
import com.example.aiticket.ai.rag.domain.AiMessageRole;

import java.time.LocalDateTime;
import java.util.List;

public record AiMessageResponse(
        Long id,
        Long sessionId,
        AiMessageRole role,
        String content,
        String modelName,
        Boolean canAnswer,
        Double confidence,
        Boolean transferSuggested,
        String transferReason,
        LocalDateTime createdAt,
        List<RagCitationResponse> citations
) {
    public static AiMessageResponse from(AiMessageWithCitations message) {
        return new AiMessageResponse(
                message.message().id(),
                message.message().sessionId(),
                message.message().role(),
                message.message().content(),
                message.message().modelName(),
                message.message().canAnswer(),
                message.message().confidence(),
                message.message().transferSuggested(),
                message.message().transferReason(),
                message.message().createdAt(),
                message.citations().stream().map(RagCitationResponse::from).toList()
        );
    }
}

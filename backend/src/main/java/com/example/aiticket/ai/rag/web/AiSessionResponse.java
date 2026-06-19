package com.example.aiticket.ai.rag.web;

import com.example.aiticket.ai.rag.domain.AiSession;

import java.time.LocalDateTime;

public record AiSessionResponse(
        Long id,
        String title,
        String lastQuestion,
        Boolean transferSuggested,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AiSessionResponse from(AiSession session) {
        return new AiSessionResponse(
                session.id(),
                session.title(),
                session.lastQuestion(),
                session.transferSuggested(),
                session.createdAt(),
                session.updatedAt()
        );
    }
}

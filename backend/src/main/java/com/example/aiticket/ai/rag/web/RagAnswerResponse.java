package com.example.aiticket.ai.rag.web;

import com.example.aiticket.ai.rag.domain.RagAnswer;

import java.util.List;

public record RagAnswerResponse(
        Long sessionId,
        Long userMessageId,
        Long assistantMessageId,
        String answer,
        boolean canAnswer,
        double confidence,
        boolean transferSuggested,
        String transferReason,
        List<RagCitationResponse> citations
) {
    public static RagAnswerResponse from(RagAnswer answer) {
        return new RagAnswerResponse(
                answer.sessionId(),
                answer.userMessageId(),
                answer.assistantMessageId(),
                answer.answer(),
                answer.canAnswer(),
                answer.confidence(),
                answer.transferSuggested(),
                answer.transferReason(),
                answer.citations().stream().map(RagCitationResponse::from).toList()
        );
    }
}

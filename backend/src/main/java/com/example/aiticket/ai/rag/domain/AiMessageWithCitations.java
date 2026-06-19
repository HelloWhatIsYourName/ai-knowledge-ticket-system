package com.example.aiticket.ai.rag.domain;

import java.util.List;

public record AiMessageWithCitations(
        AiMessage message,
        List<AiMessageCitation> citations
) {
}

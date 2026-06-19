package com.example.aiticket.ai.rag.domain;

import java.util.List;

public record RagPrompt(
        String prompt,
        List<RagCitation> citations
) {
}

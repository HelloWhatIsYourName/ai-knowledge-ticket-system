package com.example.aiticket.ai.rag.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskQuestionRequest(
        @NotBlank @Size(max = 2000) String question,
        Long sessionId,
        Long categoryId,
        Integer topK,
        Double minSimilarity
) {
}

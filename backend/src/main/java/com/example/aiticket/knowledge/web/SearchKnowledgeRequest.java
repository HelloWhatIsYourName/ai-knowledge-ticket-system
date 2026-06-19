package com.example.aiticket.knowledge.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SearchKnowledgeRequest(
        @NotBlank String query,
        Long categoryId,
        @Min(1) @Max(20) Integer topK,
        @Min(0) @Max(1) Double minSimilarity
) {
}

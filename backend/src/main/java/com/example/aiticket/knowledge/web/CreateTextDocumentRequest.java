package com.example.aiticket.knowledge.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTextDocumentRequest(
        @NotBlank @Size(max = 200) String title,
        Long categoryId,
        @NotBlank @Size(max = 200_000) String content
) {
}

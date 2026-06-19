package com.example.aiticket.ai.chat;

public record ChatResult(
        String model,
        String content,
        boolean canAnswer,
        double confidence,
        String reason
) {
}

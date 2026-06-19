package com.example.aiticket.knowledge.domain;

public record TextChunk(int chunkIndex, String content) {
    public TextChunk {
        if (chunkIndex < 0) {
            throw new IllegalArgumentException("chunkIndex must be non-negative");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
    }
}

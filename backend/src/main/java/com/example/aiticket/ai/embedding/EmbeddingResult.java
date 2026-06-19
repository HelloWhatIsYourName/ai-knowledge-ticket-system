package com.example.aiticket.ai.embedding;

import java.util.List;

public record EmbeddingResult(
        String model,
        int dimensions,
        List<Float> vector
) {
    public EmbeddingResult {
        if (dimensions <= 0) {
            throw new IllegalArgumentException("dimensions must be positive");
        }
        if (vector == null || vector.size() != dimensions) {
            throw new IllegalArgumentException("vector size must match dimensions");
        }
    }
}

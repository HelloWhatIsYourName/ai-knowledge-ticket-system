package com.example.aiticket.ai.embedding;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmbeddingResultTest {
    @Test
    void rejectsVectorWithWrongDimensions() {
        assertThatThrownBy(() -> new EmbeddingResult("text-embedding-v3", 1024, List.of(0.1f, 0.2f)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("vector size must match dimensions");
    }
}

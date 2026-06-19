package com.example.aiticket.ai.embedding;

import java.util.List;

public interface EmbeddingClient {
    EmbeddingResult embed(String text);

    List<EmbeddingResult> embedBatch(List<String> texts);
}

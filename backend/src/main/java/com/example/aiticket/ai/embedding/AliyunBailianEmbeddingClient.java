package com.example.aiticket.ai.embedding;

import com.example.aiticket.config.AiProviderProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;

@Component
public class AliyunBailianEmbeddingClient implements EmbeddingClient {
    private final AiProviderProperties properties;
    private final RestClient restClient;

    public AliyunBailianEmbeddingClient(AiProviderProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.restClient = builder
                .baseUrl(properties.getEmbedding().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getEmbedding().getApiKey())
                .build();
    }

    @Override
    public EmbeddingResult embed(String text) {
        List<EmbeddingResult> results = embedBatch(List.of(text));
        return results.getFirst();
    }

    @Override
    public List<EmbeddingResult> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("texts must not be empty");
        }

        EmbeddingRequest request = new EmbeddingRequest(
                properties.getEmbedding().getModel(),
                texts,
                properties.getEmbedding().getDimensions()
        );

        EmbeddingResponse response = restClient.post()
                .uri("/embeddings")
                .body(request)
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.data() == null || response.data().size() != texts.size()) {
            throw new IllegalStateException("embedding response size does not match request size");
        }

        return response.data().stream()
                .sorted(Comparator.comparingInt(EmbeddingData::index))
                .map(item -> new EmbeddingResult(
                        properties.getEmbedding().getModel(),
                        properties.getEmbedding().getDimensions(),
                        item.embedding()
                ))
                .toList();
    }

    private record EmbeddingRequest(String model, List<String> input, Integer dimensions) {
    }

    private record EmbeddingResponse(List<EmbeddingData> data) {
    }

    private record EmbeddingData(int index, List<Float> embedding) {
    }
}

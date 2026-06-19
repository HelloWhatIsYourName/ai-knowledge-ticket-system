package com.example.aiticket.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgePropertiesTest {
    @Test
    void bindsKnowledgeDefaultsAndOverrides() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "knowledge.chunk.max-chars", "640",
                "knowledge.chunk.overlap-chars", "96",
                "knowledge.retrieval.top-k", "6",
                "knowledge.retrieval.min-similarity", "0.72",
                "knowledge.parse.max-retry-count", "3",
                "knowledge.queue.stream-key", "stream:kb:parse",
                "knowledge.queue.consumer-group", "kb-parser-group"
        ));

        BindResult<KnowledgeProperties> result = new Binder(source)
                .bind("knowledge", KnowledgeProperties.class);

        assertThat(result.isBound()).isTrue();
        KnowledgeProperties properties = result.get();
        assertThat(properties.getChunk().getMaxChars()).isEqualTo(640);
        assertThat(properties.getChunk().getOverlapChars()).isEqualTo(96);
        assertThat(properties.getRetrieval().getTopK()).isEqualTo(6);
        assertThat(properties.getRetrieval().getMinSimilarity()).isEqualTo(0.72);
        assertThat(properties.getParse().getMaxRetryCount()).isEqualTo(3);
        assertThat(properties.getQueue().getStreamKey()).isEqualTo("stream:kb:parse");
        assertThat(properties.getQueue().getConsumerGroup()).isEqualTo("kb-parser-group");
    }

    @Test
    void providesSafeDefaults() {
        KnowledgeProperties properties = new KnowledgeProperties();

        assertThat(properties.getChunk().getMaxChars()).isEqualTo(700);
        assertThat(properties.getChunk().getOverlapChars()).isEqualTo(100);
        assertThat(properties.getRetrieval().getTopK()).isEqualTo(5);
        assertThat(properties.getRetrieval().getMinSimilarity()).isEqualTo(0.70);
        assertThat(properties.getParse().getMaxRetryCount()).isEqualTo(3);
        assertThat(properties.getQueue().getStreamKey()).isEqualTo("stream:kb:parse");
        assertThat(properties.getQueue().getConsumerGroup()).isEqualTo("kb-parser-group");
    }

    @Test
    void rejectsUnsafeChunkAndRetrievalSettings() {
        KnowledgeProperties properties = new KnowledgeProperties();
        properties.getChunk().setMaxChars(700);
        properties.getChunk().setOverlapChars(700);
        properties.getRetrieval().setTopK(100);
        properties.getParse().setMaxRetryCount(100);

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "chunk.overlapSmallerThanMaxChars",
                        "retrieval.topK",
                        "parse.maxRetryCount"
                );
    }
}

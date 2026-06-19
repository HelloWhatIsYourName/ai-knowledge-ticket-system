package com.example.aiticket.knowledge.service;

import com.example.aiticket.ai.embedding.EmbeddingClient;
import com.example.aiticket.ai.embedding.EmbeddingResult;
import com.example.aiticket.config.KnowledgeProperties;
import com.example.aiticket.knowledge.domain.KnowledgeSearchResult;
import com.example.aiticket.knowledge.mapper.KnowledgeChunkMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeRetrievalServiceTest {
    @Test
    void usesDefaultRetrievalSettingsAndEmbedsQuery() {
        FakeChunkMapper chunkMapper = new FakeChunkMapper();
        FakeEmbeddingClient embeddingClient = new FakeEmbeddingClient();
        KnowledgeRetrievalService service = new KnowledgeRetrievalService(
                chunkMapper,
                embeddingClient,
                new KnowledgeProperties()
        );

        service.search("如何重置密码", null, null, null);

        assertThat(embeddingClient.embeddedTexts).containsExactly("如何重置密码");
        assertThat(chunkMapper.queryVectorLiteral).startsWith("[");
        assertThat(chunkMapper.categoryId).isNull();
        assertThat(chunkMapper.minSimilarity).isEqualTo(0.70);
        assertThat(chunkMapper.limit).isEqualTo(5);
    }

    private static final class FakeEmbeddingClient implements EmbeddingClient {
        private final List<String> embeddedTexts = new ArrayList<>();

        @Override
        public EmbeddingResult embed(String text) {
            embeddedTexts.add(text);
            return new EmbeddingResult("fake-model", 1024, vector());
        }

        @Override
        public List<EmbeddingResult> embedBatch(List<String> texts) {
            return texts.stream().map(this::embed).toList();
        }

        private static List<Float> vector() {
            List<Float> values = new ArrayList<>();
            for (int i = 0; i < 1024; i++) {
                values.add(i == 0 ? 1.0f : 0.0f);
            }
            return values;
        }
    }

    private static final class FakeChunkMapper implements KnowledgeChunkMapper {
        private String queryVectorLiteral;
        private Long categoryId;
        private double minSimilarity;
        private int limit;

        @Override
        public int deleteByDocumentId(Long documentId) {
            return 0;
        }

        @Override
        public int insertBatchNonEmpty(List<com.example.aiticket.knowledge.domain.KnowledgeChunkDraft> chunks) {
            return chunks.size();
        }

        @Override
        public List<com.example.aiticket.knowledge.domain.KnowledgeChunk> findByDocumentId(Long documentId) {
            return List.of();
        }

        @Override
        public List<KnowledgeSearchResult> search(String queryVectorLiteral, Long categoryId, double minSimilarity, int limit) {
            this.queryVectorLiteral = queryVectorLiteral;
            this.categoryId = categoryId;
            this.minSimilarity = minSimilarity;
            this.limit = limit;
            return List.of();
        }
    }
}

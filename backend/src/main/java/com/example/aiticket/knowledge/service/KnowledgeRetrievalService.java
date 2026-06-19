package com.example.aiticket.knowledge.service;

import com.example.aiticket.ai.embedding.EmbeddingClient;
import com.example.aiticket.ai.embedding.EmbeddingResult;
import com.example.aiticket.config.KnowledgeProperties;
import com.example.aiticket.knowledge.domain.KnowledgeSearchResult;
import com.example.aiticket.knowledge.mapper.KnowledgeChunkMapper;
import com.example.aiticket.vector.OracleVectorLiteral;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeRetrievalService {
    private final KnowledgeChunkMapper chunkMapper;
    private final EmbeddingClient embeddingClient;
    private final KnowledgeProperties properties;

    public KnowledgeRetrievalService(KnowledgeChunkMapper chunkMapper,
                                     EmbeddingClient embeddingClient,
                                     KnowledgeProperties properties) {
        this.chunkMapper = chunkMapper;
        this.embeddingClient = embeddingClient;
        this.properties = properties;
    }

    public List<KnowledgeSearchResult> search(String query, Long categoryId, Integer topK, Double minSimilarity) {
        int resolvedTopK = topK == null ? properties.getRetrieval().getTopK() : topK;
        double resolvedMinSimilarity = minSimilarity == null
                ? properties.getRetrieval().getMinSimilarity()
                : minSimilarity;
        EmbeddingResult embedding = embeddingClient.embed(query);
        return chunkMapper.search(
                OracleVectorLiteral.from(embedding.vector()),
                categoryId,
                resolvedMinSimilarity,
                resolvedTopK
        );
    }
}

package com.example.aiticket.vector;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VectorSpikeService {
    private final VectorSpikeMapper mapper;

    public VectorSpikeService(VectorSpikeMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public void insert(String content, List<Float> embedding) {
        mapper.insert(content, OracleVectorLiteral.from(embedding));
    }

    public List<VectorSpikeRecord> search(List<Float> queryEmbedding, int limit) {
        return mapper.search(OracleVectorLiteral.from(queryEmbedding), limit);
    }
}

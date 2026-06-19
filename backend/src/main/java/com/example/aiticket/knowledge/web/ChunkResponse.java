package com.example.aiticket.knowledge.web;

import com.example.aiticket.knowledge.domain.KnowledgeChunk;

public record ChunkResponse(
        Long id,
        Integer chunkIndex,
        String content,
        String contentHash,
        String sourceTitle
) {
    public static ChunkResponse from(KnowledgeChunk chunk) {
        return new ChunkResponse(
                chunk.id(),
                chunk.chunkIndex(),
                chunk.content(),
                chunk.contentHash(),
                chunk.sourceTitle()
        );
    }
}

package com.example.aiticket.knowledge.mapper;

import com.example.aiticket.knowledge.domain.KnowledgeChunk;
import com.example.aiticket.knowledge.domain.KnowledgeChunkDraft;
import com.example.aiticket.knowledge.domain.KnowledgeSearchResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeChunkMapper {
    int deleteByDocumentId(@Param("documentId") Long documentId);

    default int insertBatch(List<KnowledgeChunkDraft> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return 0;
        }
        return insertBatchNonEmpty(chunks);
    }

    int insertBatchNonEmpty(@Param("chunks") List<KnowledgeChunkDraft> chunks);

    List<KnowledgeChunk> findByDocumentId(@Param("documentId") Long documentId);

    List<KnowledgeSearchResult> search(@Param("queryVectorLiteral") String queryVectorLiteral,
                                       @Param("categoryId") Long categoryId,
                                       @Param("minSimilarity") double minSimilarity,
                                       @Param("limit") int limit);
}

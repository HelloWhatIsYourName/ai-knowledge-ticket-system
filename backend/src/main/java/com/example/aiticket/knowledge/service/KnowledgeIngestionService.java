package com.example.aiticket.knowledge.service;

import com.example.aiticket.ai.embedding.EmbeddingClient;
import com.example.aiticket.ai.embedding.EmbeddingResult;
import com.example.aiticket.config.AiProviderProperties;
import com.example.aiticket.config.KnowledgeProperties;
import com.example.aiticket.knowledge.chunk.TextChunker;
import com.example.aiticket.knowledge.domain.KnowledgeChunkDraft;
import com.example.aiticket.knowledge.domain.KnowledgeParseStatus;
import com.example.aiticket.knowledge.domain.TextChunk;
import com.example.aiticket.knowledge.mapper.KnowledgeChunkMapper;
import com.example.aiticket.knowledge.mapper.KnowledgeDocumentMapper;
import com.example.aiticket.vector.OracleVectorLiteral;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class KnowledgeIngestionService {
    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;
    private final TextChunker textChunker;
    private final EmbeddingClient embeddingClient;
    private final KnowledgeProperties knowledgeProperties;
    private final AiProviderProperties aiProviderProperties;

    public KnowledgeIngestionService(KnowledgeDocumentMapper documentMapper,
                                     KnowledgeChunkMapper chunkMapper,
                                     TextChunker textChunker,
                                     EmbeddingClient embeddingClient,
                                     KnowledgeProperties knowledgeProperties,
                                     AiProviderProperties aiProviderProperties) {
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.textChunker = textChunker;
        this.embeddingClient = embeddingClient;
        this.knowledgeProperties = knowledgeProperties;
        this.aiProviderProperties = aiProviderProperties;
    }

    @Transactional
    public void ingestText(Long documentId, String title, Long categoryId, String text) {
        documentMapper.updateParseStatus(documentId, KnowledgeParseStatus.PARSING, null);
        try {
            List<TextChunk> chunks = textChunker.chunk(text);
            chunkMapper.deleteByDocumentId(documentId);
            if (chunks.isEmpty()) {
                chunkMapper.insertBatch(List.of());
                documentMapper.updateParseStatus(documentId, KnowledgeParseStatus.PARSE_SUCCESS, null);
                return;
            }

            List<String> texts = chunks.stream().map(TextChunk::content).toList();
            List<EmbeddingResult> embeddings = embeddingClient.embedBatch(texts);
            if (embeddings.size() != chunks.size()) {
                throw new TerminalIngestionException("embedding result count does not match chunk count");
            }

            List<KnowledgeChunkDraft> drafts = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                TextChunk chunk = chunks.get(i);
                EmbeddingResult embedding = embeddings.get(i);
                if (embedding.vector().size() != aiProviderProperties.getEmbedding().getDimensions()) {
                    throw new TerminalIngestionException("embedding dimensions mismatch");
                }
                drafts.add(new KnowledgeChunkDraft(
                        documentId,
                        categoryId,
                        chunk.chunkIndex(),
                        chunk.content(),
                        sha256(chunk.content()),
                        title,
                        null,
                        OracleVectorLiteral.from(embedding.vector())
                ));
            }

            chunkMapper.insertBatch(drafts);
            documentMapper.updateParseStatus(documentId, KnowledgeParseStatus.PARSE_SUCCESS, null);
        } catch (TerminalIngestionException ex) {
            documentMapper.markParseFailedTerminal(documentId, ex.getMessage());
            throw ex;
        } catch (RuntimeException ex) {
            documentMapper.markParseFailed(documentId, ex.getMessage(), knowledgeProperties.getParse().getMaxRetryCount());
            throw ex;
        }
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private static class TerminalIngestionException extends IllegalStateException {
        TerminalIngestionException(String message) {
            super(message);
        }
    }
}

package com.example.aiticket.ai.rag.mapper;

import com.example.aiticket.ai.rag.domain.AiMessage;
import com.example.aiticket.ai.rag.domain.AiMessageCitation;
import com.example.aiticket.ai.rag.domain.AiMessageRole;
import com.example.aiticket.ai.rag.domain.AiSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiChatMapper {
    Long nextSessionId();

    Long nextMessageId();

    Long nextCitationId();

    int insertSession(@Param("id") Long id,
                      @Param("userId") Long userId,
                      @Param("title") String title,
                      @Param("lastQuestion") String lastQuestion);

    int updateSessionSummary(@Param("id") Long id,
                             @Param("userId") Long userId,
                             @Param("lastQuestion") String lastQuestion,
                             @Param("transferSuggested") Integer transferSuggested);

    AiSession findOwnedSession(@Param("id") Long id, @Param("userId") Long userId);

    List<AiSession> listOwnedSessions(@Param("userId") Long userId, @Param("limit") int limit);

    int insertMessage(@Param("id") Long id,
                      @Param("sessionId") Long sessionId,
                      @Param("userId") Long userId,
                      @Param("role") AiMessageRole role,
                      @Param("content") String content,
                      @Param("modelName") String modelName,
                      @Param("canAnswer") Integer canAnswer,
                      @Param("confidence") Double confidence,
                      @Param("transferSuggested") Integer transferSuggested,
                      @Param("transferReason") String transferReason);

    int insertCitation(@Param("id") Long id,
                       @Param("messageId") Long messageId,
                       @Param("chunkId") Long chunkId,
                       @Param("documentId") Long documentId,
                       @Param("citationIndex") Integer citationIndex,
                       @Param("sourceTitle") String sourceTitle,
                       @Param("snippet") String snippet,
                       @Param("similarity") Double similarity);

    List<AiMessage> listMessages(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    List<AiMessageCitation> listCitations(@Param("messageId") Long messageId);
}

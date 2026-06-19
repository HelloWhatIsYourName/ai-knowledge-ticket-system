package com.example.aiticket.ai.rag.service;

import com.example.aiticket.ai.chat.ChatClient;
import com.example.aiticket.ai.chat.ChatResult;
import com.example.aiticket.ai.rag.domain.AiMessage;
import com.example.aiticket.ai.rag.domain.AiMessageCitation;
import com.example.aiticket.ai.rag.domain.AiMessageRole;
import com.example.aiticket.ai.rag.domain.AiMessageWithCitations;
import com.example.aiticket.ai.rag.domain.AiSession;
import com.example.aiticket.ai.rag.domain.RagAnswer;
import com.example.aiticket.ai.rag.mapper.AiChatMapper;
import com.example.aiticket.ai.rag.prompt.RagPromptBuilder;
import com.example.aiticket.config.KnowledgeProperties;
import com.example.aiticket.knowledge.domain.KnowledgeSearchResult;
import com.example.aiticket.knowledge.service.KnowledgeRetrievalService;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagChatServiceTest {
    @Test
    void askCreatesSessionPersistsMessagesAndCitations() {
        FakeMapper mapper = new FakeMapper();
        RagChatService service = service(mapper, new FakeRetrievalService(List.of(result())), new FakeChatClient());

        RagAnswer answer = service.ask(7L, null, "忘记密码怎么处理？", 1L, 5, 0.7);

        assertThat(answer.sessionId()).isEqualTo(100L);
        assertThat(answer.userMessageId()).isEqualTo(200L);
        assertThat(answer.assistantMessageId()).isEqualTo(201L);
        assertThat(answer.answer()).isEqualTo("可以在登录页点击忘记密码。");
        assertThat(answer.canAnswer()).isTrue();
        assertThat(answer.citations()).hasSize(1);
        assertThat(mapper.sessions).hasSize(1);
        assertThat(mapper.messages).extracting(AiMessage::role)
                .containsExactly(AiMessageRole.USER, AiMessageRole.ASSISTANT);
        assertThat(mapper.citations).hasSize(1);
        assertThat(mapper.citations.getFirst().messageId()).isEqualTo(201L);
        assertThat(mapper.updatedTransferSuggested).isFalse();
    }

    @Test
    void askRejectsSessionOwnedByAnotherUser() {
        FakeMapper mapper = new FakeMapper();
        mapper.ownedSession = null;
        RagChatService service = service(mapper, new FakeRetrievalService(List.of(result())), new FakeChatClient());

        assertThatThrownBy(() -> service.ask(7L, 999L, "问题", null, null, null))
                .isInstanceOf(RagSessionNotFoundException.class)
                .hasMessage("AI session not found");

        assertThat(mapper.messages).isEmpty();
    }

    @Test
    void askDoesNotPersistFakeAssistantMessageWhenChatFails() {
        FakeMapper mapper = new FakeMapper();
        RagChatService service = service(mapper, new FakeRetrievalService(List.of(result())), new FailingChatClient());

        assertThatThrownBy(() -> service.ask(7L, null, "问题", null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("chat failed");

        assertThat(mapper.messages).extracting(AiMessage::role)
                .containsExactly(AiMessageRole.USER);
        assertThat(mapper.citations).isEmpty();
    }

    @Test
    void listsOwnedMessagesWithCitations() {
        FakeMapper mapper = new FakeMapper();
        mapper.messages.add(new AiMessage(1L, 10L, 7L, AiMessageRole.ASSISTANT, "回答",
                "deepseek-chat", true, 0.8, false, null, LocalDateTime.now()));
        mapper.citations.add(new AiMessageCitation(3L, 1L, 11L, 21L, 1,
                "密码重置操作指南", "片段", 0.8, LocalDateTime.now()));
        RagChatService service = service(mapper, new FakeRetrievalService(List.of(result())), new FakeChatClient());

        List<AiMessageWithCitations> messages = service.listMessages(7L, 10L);

        assertThat(messages).hasSize(1);
        assertThat(messages.getFirst().message().id()).isEqualTo(1L);
        assertThat(messages.getFirst().citations()).hasSize(1);
    }

    private RagChatService service(FakeMapper mapper, KnowledgeRetrievalService retrievalService, ChatClient chatClient) {
        return new RagChatService(
                mapper,
                retrievalService,
                chatClient,
                new RagPromptBuilder(),
                new RagAnswerPolicy(),
                new KnowledgeProperties()
        );
    }

    private KnowledgeSearchResult result() {
        return new KnowledgeSearchResult(11L, 21L, 1L, 0,
                "用户可以在登录页点击忘记密码。", "密码重置操作指南", 0.2, 0.8);
    }

    private static final class FakeRetrievalService extends KnowledgeRetrievalService {
        private final List<KnowledgeSearchResult> results;

        private FakeRetrievalService(List<KnowledgeSearchResult> results) {
            super(null, null, null);
            this.results = results;
        }

        @Override
        public List<KnowledgeSearchResult> search(String query, Long categoryId, Integer topK, Double minSimilarity) {
            return results;
        }
    }

    private static class FakeChatClient implements ChatClient {
        @Override
        public ChatResult chat(String prompt) {
            return new ChatResult("deepseek-chat", "可以在登录页点击忘记密码。", true, 0.8, null);
        }

        @Override
        public SseEmitter streamChat(String prompt) {
            return new SseEmitter();
        }
    }

    private static final class FailingChatClient extends FakeChatClient {
        @Override
        public ChatResult chat(String prompt) {
            throw new IllegalStateException("chat failed");
        }
    }

    private static final class FakeMapper implements AiChatMapper {
        private long nextSessionId = 100L;
        private long nextMessageId = 200L;
        private long nextCitationId = 300L;
        private AiSession ownedSession = new AiSession(999L, 7L, "已有会话", null, false,
                LocalDateTime.now(), LocalDateTime.now());
        private final List<AiSession> sessions = new ArrayList<>();
        private final List<AiMessage> messages = new ArrayList<>();
        private final List<AiMessageCitation> citations = new ArrayList<>();
        private boolean updatedTransferSuggested;

        @Override
        public Long nextSessionId() {
            return nextSessionId++;
        }

        @Override
        public Long nextMessageId() {
            return nextMessageId++;
        }

        @Override
        public Long nextCitationId() {
            return nextCitationId++;
        }

        @Override
        public int insertSession(Long id, Long userId, String title, String lastQuestion) {
            sessions.add(new AiSession(id, userId, title, lastQuestion, false,
                    LocalDateTime.now(), LocalDateTime.now()));
            return 1;
        }

        @Override
        public int updateSessionSummary(Long id, Long userId, String lastQuestion, Integer transferSuggested) {
            this.updatedTransferSuggested = transferSuggested == 1;
            return 1;
        }

        @Override
        public AiSession findOwnedSession(Long id, Long userId) {
            if (ownedSession != null && ownedSession.id().equals(id) && ownedSession.userId().equals(userId)) {
                return ownedSession;
            }
            return null;
        }

        @Override
        public List<AiSession> listOwnedSessions(Long userId, int limit) {
            return sessions;
        }

        @Override
        public int insertMessage(Long id, Long sessionId, Long userId, AiMessageRole role, String content,
                                 String modelName, Integer canAnswer, Double confidence,
                                 Integer transferSuggested, String transferReason) {
            messages.add(new AiMessage(id, sessionId, userId, role, content, modelName,
                    canAnswer == null ? null : canAnswer == 1,
                    confidence, transferSuggested != null && transferSuggested == 1,
                    transferReason, LocalDateTime.now()));
            return 1;
        }

        @Override
        public int insertCitation(Long id, Long messageId, Long chunkId, Long documentId, Integer citationIndex,
                                  String sourceTitle, String snippet, Double similarity) {
            citations.add(new AiMessageCitation(id, messageId, chunkId, documentId, citationIndex,
                    sourceTitle, snippet, similarity, LocalDateTime.now()));
            return 1;
        }

        @Override
        public List<AiMessage> listMessages(Long sessionId, Long userId) {
            return messages;
        }

        @Override
        public List<AiMessageCitation> listCitations(Long messageId) {
            return citations.stream()
                    .filter(citation -> citation.messageId().equals(messageId))
                    .toList();
        }
    }
}

package com.example.aiticket.ai.rag.web;

import com.example.aiticket.ai.rag.domain.AiMessage;
import com.example.aiticket.ai.rag.domain.AiMessageCitation;
import com.example.aiticket.ai.rag.domain.AiMessageRole;
import com.example.aiticket.ai.rag.domain.AiMessageWithCitations;
import com.example.aiticket.ai.rag.domain.AiSession;
import com.example.aiticket.ai.rag.domain.RagAnswer;
import com.example.aiticket.ai.rag.domain.RagCitation;
import com.example.aiticket.ai.rag.service.RagChatService;
import com.example.aiticket.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagChatControllerTest {
    @Test
    void endpointsKeepExpectedPermissions() throws Exception {
        assertThat(method("ask", AskQuestionRequest.class, AuthenticatedUser.class)
                .getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('ai:chat:ask')");
        assertThat(method("stream", String.class, Long.class, Long.class, Integer.class, Double.class,
                AuthenticatedUser.class).getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('ai:chat:ask')");
        assertThat(method("sessions", AuthenticatedUser.class).getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('ai:chat:history:view')");
        assertThat(method("messages", Long.class, AuthenticatedUser.class).getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('ai:chat:history:view')");
    }

    @Test
    void askMapsServiceAnswerToResponseWithoutVectors() {
        RagChatController controller = new RagChatController(new FakeRagChatService());

        RagAnswerResponse response = controller.ask(
                new AskQuestionRequest("忘记密码怎么处理？", null, 1L, 5, 0.7),
                user()
        ).data();

        assertThat(response.sessionId()).isEqualTo(10L);
        assertThat(response.answer()).isEqualTo("可以在登录页点击忘记密码。");
        assertThat(response.canAnswer()).isTrue();
        assertThat(response.citations()).hasSize(1);
        assertThat(response.citations().getFirst().sourceTitle()).isEqualTo("密码重置操作指南");
    }

    @Test
    void streamReturnsSseEmitter() {
        RagChatController controller = new RagChatController(new FakeRagChatService());

        SseEmitter emitter = controller.stream("忘记密码怎么处理？", null, 1L, 5, 0.7, user());

        assertThat(emitter).isNotNull();
    }

    @Test
    void historyMapsSessionsAndMessages() {
        RagChatController controller = new RagChatController(new FakeRagChatService());

        assertThat(controller.sessions(user()).data()).hasSize(1);
        assertThat(controller.messages(10L, user()).data()).hasSize(1);
        assertThat(controller.messages(10L, user()).data().getFirst().citations()).hasSize(1);
    }

    private Method method(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return RagChatController.class.getMethod(name, parameterTypes);
    }

    private AuthenticatedUser user() {
        return new AuthenticatedUser(7L, "user", "User", 1,
                List.of("USER"), List.of("ai:chat:ask", "ai:chat:history:view"));
    }

    private static final class FakeRagChatService extends RagChatService {
        private FakeRagChatService() {
            super(null, null, null, null, null, null);
        }

        @Override
        public RagAnswer ask(Long userId, Long sessionId, String question, Long categoryId,
                             Integer topK, Double minSimilarity) {
            return new RagAnswer(10L, 20L, 21L, "可以在登录页点击忘记密码。",
                    true, 0.8, false, null, List.of(
                    new RagCitation(1, 11L, 12L, 1L,
                            "密码重置操作指南", 0.8, "用户可以在登录页点击忘记密码。")
            ));
        }

        @Override
        public List<AiSession> listSessions(Long userId, int limit) {
            return List.of(new AiSession(10L, userId, "忘记密码", "忘记密码怎么处理？",
                    false, LocalDateTime.now(), LocalDateTime.now()));
        }

        @Override
        public List<AiMessageWithCitations> listMessages(Long userId, Long sessionId) {
            AiMessage message = new AiMessage(21L, sessionId, userId, AiMessageRole.ASSISTANT,
                    "可以在登录页点击忘记密码。", "deepseek-chat", true, 0.8,
                    false, null, LocalDateTime.now());
            AiMessageCitation citation = new AiMessageCitation(31L, 21L, 11L, 12L,
                    1, "密码重置操作指南", "用户可以在登录页点击忘记密码。", 0.8,
                    LocalDateTime.now());
            return List.of(new AiMessageWithCitations(message, List.of(citation)));
        }
    }
}

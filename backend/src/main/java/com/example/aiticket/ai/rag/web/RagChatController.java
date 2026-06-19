package com.example.aiticket.ai.rag.web;

import com.example.aiticket.ai.rag.domain.RagAnswer;
import com.example.aiticket.ai.rag.service.RagChatService;
import com.example.aiticket.common.api.ApiResponse;
import com.example.aiticket.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/ai/chat")
public class RagChatController {
    private final RagChatService chatService;

    public RagChatController(RagChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/ask")
    @PreAuthorize("hasAuthority('ai:chat:ask')")
    public ApiResponse<RagAnswerResponse> ask(@Valid @RequestBody AskQuestionRequest request,
                                              @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(RagAnswerResponse.from(chatService.ask(
                user.id(),
                request.sessionId(),
                request.question(),
                request.categoryId(),
                request.topK(),
                request.minSimilarity()
        )));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('ai:chat:ask')")
    public SseEmitter stream(@RequestParam String question,
                             @RequestParam(required = false) Long sessionId,
                             @RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) Integer topK,
                             @RequestParam(required = false) Double minSimilarity,
                             @AuthenticationPrincipal AuthenticatedUser user) {
        SseEmitter emitter = new SseEmitter();
        Thread.startVirtualThread(() -> {
            try {
                RagAnswerResponse response = RagAnswerResponse.from(chatService.ask(
                        user.id(), sessionId, question, categoryId, topK, minSimilarity));
                for (String token : splitForStreaming(response.answer())) {
                    emitter.send(SseEmitter.event().name("token").data(token));
                }
                emitter.send(SseEmitter.event().name("metadata").data(response));
                emitter.complete();
            } catch (Exception ex) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("rag chat failed"));
                } catch (IOException ignored) {
                    // Client already disconnected.
                }
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    @GetMapping("/sessions")
    @PreAuthorize("hasAuthority('ai:chat:history:view')")
    public ApiResponse<List<AiSessionResponse>> sessions(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(chatService.listSessions(user.id(), 100).stream()
                .map(AiSessionResponse::from)
                .toList());
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @PreAuthorize("hasAuthority('ai:chat:history:view')")
    public ApiResponse<List<AiMessageResponse>> messages(@PathVariable Long sessionId,
                                                         @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(chatService.listMessages(user.id(), sessionId).stream()
                .map(AiMessageResponse::from)
                .toList());
    }

    private List<String> splitForStreaming(String content) {
        if (content == null || content.isBlank()) {
            return List.of("");
        }
        if (content.length() <= 24) {
            return List.of(content);
        }
        return java.util.stream.IntStream.iterate(0, index -> index < content.length(), index -> index + 24)
                .mapToObj(index -> content.substring(index, Math.min(index + 24, content.length())))
                .toList();
    }
}

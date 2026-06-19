package com.example.aiticket.ai.chat;

import com.example.aiticket.config.AiProviderProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Component
public class OpenAiCompatibleChatClient implements ChatClient {
    private static final double DEFAULT_CONFIDENCE = 0.7;

    private final AiProviderProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiCompatibleChatClient(AiProviderProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.restClient = builder
                .baseUrl(properties.getChat().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getChat().getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public ChatResult chat(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt must not be blank");
        }

        ChatCompletionResponse response = restClient.post()
                .uri("/chat/completions")
                .body(new ChatCompletionRequest(
                        properties.getChat().getModel(),
                        List.of(new ChatMessage("user", prompt))
                ))
                .retrieve()
                .body(ChatCompletionResponse.class);

        String content = extractContent(response);
        return parseResult(content);
    }

    @Override
    public SseEmitter streamChat(String prompt) {
        SseEmitter emitter = new SseEmitter();
        Thread.startVirtualThread(() -> {
            try {
                ChatResult result = chat(prompt);
                for (String token : splitForStreaming(result.content())) {
                    emitter.send(SseEmitter.event().name("token").data(token));
                }
                emitter.send(SseEmitter.event().name("metadata").data(result));
                emitter.complete();
            } catch (Exception ex) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("chat provider failed"));
                } catch (IOException ignored) {
                    // The client may have already gone away.
                }
                emitter.complete();
            }
        });
        return emitter;
    }

    private String extractContent(ChatCompletionResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("chat response does not contain an answer");
        }
        ChatChoice first = response.choices().getFirst();
        if (first.message() == null || first.message().content() == null || first.message().content().isBlank()) {
            throw new IllegalStateException("chat response does not contain an answer");
        }
        return first.message().content();
    }

    private ChatResult parseResult(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                StructuredAnswer answer = objectMapper.readValue(trimmed, StructuredAnswer.class);
                if (answer.answer() != null && !answer.answer().isBlank()) {
                    return new ChatResult(
                            properties.getChat().getModel(),
                            answer.answer(),
                            answer.canAnswer() == null || answer.canAnswer(),
                            answer.confidence() == null ? DEFAULT_CONFIDENCE : answer.confidence(),
                            answer.reason()
                    );
                }
            } catch (IOException ignored) {
                // Treat malformed structured content as plain answer text.
            }
        }
        return new ChatResult(properties.getChat().getModel(), content, true, DEFAULT_CONFIDENCE, null);
    }

    private List<String> splitForStreaming(String content) {
        if (content.length() <= 24) {
            return List.of(content);
        }
        return content.chars()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString()
                .lines()
                .flatMap(line -> {
                    if (line.length() <= 24) {
                        return List.of(line).stream();
                    }
                    return java.util.stream.IntStream.iterate(0, i -> i < line.length(), i -> i + 24)
                            .mapToObj(i -> line.substring(i, Math.min(i + 24, line.length())));
                })
                .toList();
    }

    private record ChatCompletionRequest(String model, List<ChatMessage> messages) {
    }

    private record ChatMessage(String role, String content) {
    }

    private record ChatCompletionResponse(List<ChatChoice> choices) {
    }

    private record ChatChoice(ChatMessage message) {
    }

    private record StructuredAnswer(
            String answer,
            @JsonProperty("can_answer") Boolean canAnswer,
            Double confidence,
            String reason
    ) {
    }
}

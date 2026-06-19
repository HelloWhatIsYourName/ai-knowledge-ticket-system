package com.example.aiticket.ai.chat;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatClient {
    ChatResult chat(String prompt);

    SseEmitter streamChat(String prompt);
}

package com.example.aiticket.knowledge.queue;

public interface KnowledgeParseQueue {
    void enqueueParseAndEmbed(Long documentId, int retryCount);
}

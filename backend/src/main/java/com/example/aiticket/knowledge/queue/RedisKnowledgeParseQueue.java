package com.example.aiticket.knowledge.queue;

import com.example.aiticket.config.KnowledgeProperties;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class RedisKnowledgeParseQueue implements KnowledgeParseQueue {
    private final StringRedisTemplate redisTemplate;
    private final KnowledgeProperties properties;

    public RedisKnowledgeParseQueue(StringRedisTemplate redisTemplate, KnowledgeProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public void enqueueParseAndEmbed(Long documentId, int retryCount) {
        Map<String, String> body = Map.of(
                "documentId", String.valueOf(documentId),
                "action", "PARSE_AND_EMBED",
                "retryCount", String.valueOf(retryCount),
                "createdAt", Instant.now().toString()
        );
        redisTemplate.opsForStream().add(MapRecord.create(properties.getQueue().getStreamKey(), body));
    }
}

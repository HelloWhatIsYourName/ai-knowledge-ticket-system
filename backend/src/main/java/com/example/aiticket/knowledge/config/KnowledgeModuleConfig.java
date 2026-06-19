package com.example.aiticket.knowledge.config;

import com.example.aiticket.config.KnowledgeProperties;
import com.example.aiticket.knowledge.chunk.ParagraphTextChunker;
import com.example.aiticket.knowledge.chunk.TextChunker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KnowledgeProperties.class)
public class KnowledgeModuleConfig {
    @Bean
    TextChunker textChunker(KnowledgeProperties properties) {
        return new ParagraphTextChunker(
                properties.getChunk().getMaxChars(),
                properties.getChunk().getOverlapChars()
        );
    }
}

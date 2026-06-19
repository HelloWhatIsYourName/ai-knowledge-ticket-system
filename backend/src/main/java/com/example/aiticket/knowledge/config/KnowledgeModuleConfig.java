package com.example.aiticket.knowledge.config;

import com.example.aiticket.config.KnowledgeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KnowledgeProperties.class)
public class KnowledgeModuleConfig {
}

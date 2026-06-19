package com.example.aiticket.knowledge.domain;

public record KnowledgeCategory(
        Long id,
        String name,
        Long parentId,
        Integer sortOrder,
        Boolean enabled
) {
}

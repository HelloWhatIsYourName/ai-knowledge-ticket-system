package com.example.aiticket.knowledge.web;

import com.example.aiticket.config.KnowledgeProperties;
import com.example.aiticket.knowledge.service.KnowledgeRetrievalService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeSearchControllerTest {
    @Test
    void searchEndpointKeepsViewPermission() throws Exception {
        Method search = KnowledgeSearchController.class.getMethod("search", SearchKnowledgeRequest.class);

        assertThat(search.getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('knowledge:document:view')");
    }

    @Test
    void requestValidationMatchesKnowledgePropertiesBounds() {
        KnowledgeProperties properties = new KnowledgeProperties();

        assertThat(properties.getRetrieval().getTopK()).isBetween(1, 20);
        assertThat(new SearchKnowledgeRequest("query", null, 20, 1.0).topK()).isEqualTo(20);
    }
}

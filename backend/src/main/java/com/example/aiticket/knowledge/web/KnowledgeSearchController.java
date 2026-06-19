package com.example.aiticket.knowledge.web;

import com.example.aiticket.common.api.ApiResponse;
import com.example.aiticket.knowledge.service.KnowledgeRetrievalService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kb")
public class KnowledgeSearchController {
    private final KnowledgeRetrievalService retrievalService;

    public KnowledgeSearchController(KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('knowledge:document:view')")
    public ApiResponse<List<SearchKnowledgeResponse>> search(@Valid @RequestBody SearchKnowledgeRequest request) {
        return ApiResponse.ok(retrievalService.search(
                        request.query(),
                        request.categoryId(),
                        request.topK(),
                        request.minSimilarity()
                ).stream()
                .map(SearchKnowledgeResponse::from)
                .toList());
    }
}

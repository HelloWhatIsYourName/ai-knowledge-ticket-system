package com.example.aiticket.knowledge.web;

import com.example.aiticket.common.api.ApiResponse;
import com.example.aiticket.knowledge.mapper.KnowledgeChunkMapper;
import com.example.aiticket.knowledge.service.KnowledgeDocumentService;
import com.example.aiticket.knowledge.service.KnowledgeIngestionService;
import com.example.aiticket.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kb/documents")
public class KnowledgeDocumentController {
    private final KnowledgeDocumentService documentService;
    private final KnowledgeIngestionService ingestionService;
    private final KnowledgeChunkMapper chunkMapper;

    public KnowledgeDocumentController(KnowledgeDocumentService documentService,
                                       KnowledgeIngestionService ingestionService,
                                       KnowledgeChunkMapper chunkMapper) {
        this.documentService = documentService;
        this.ingestionService = ingestionService;
        this.chunkMapper = chunkMapper;
    }

    @PostMapping("/text")
    @PreAuthorize("hasAuthority('knowledge:document:upload')")
    public ApiResponse<DocumentResponse> createTextDocument(@Valid @RequestBody CreateTextDocumentRequest request,
                                                            @AuthenticationPrincipal AuthenticatedUser user) {
        Long categoryId = request.categoryId() == null ? 1L : request.categoryId();
        Long documentId = documentService.createTextDocument(request.title(), categoryId, request.content(), user.id());
        try {
            ingestionService.ingestText(documentId, request.title(), categoryId, request.content());
        } catch (RuntimeException ex) {
            return ApiResponse.ok(DocumentResponse.from(documentService.getDocument(documentId)));
        }
        return ApiResponse.ok(DocumentResponse.from(documentService.getDocument(documentId)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('knowledge:document:view')")
    public ApiResponse<List<DocumentResponse>> listDocuments() {
        return ApiResponse.ok(documentService.listRecent(100).stream().map(DocumentResponse::from).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('knowledge:document:view')")
    public ApiResponse<DocumentResponse> getDocument(@PathVariable Long id) {
        return ApiResponse.ok(DocumentResponse.from(documentService.getDocument(id)));
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('knowledge:document:manage')")
    public ApiResponse<DocumentResponse> enable(@PathVariable Long id) {
        documentService.setEnabled(id, true);
        return ApiResponse.ok(DocumentResponse.from(documentService.getDocument(id)));
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('knowledge:document:manage')")
    public ApiResponse<DocumentResponse> disable(@PathVariable Long id) {
        documentService.setEnabled(id, false);
        return ApiResponse.ok(DocumentResponse.from(documentService.getDocument(id)));
    }

    @PostMapping("/{id}/retry-parse")
    @PreAuthorize("hasAuthority('knowledge:document:manage')")
    public ApiResponse<DocumentResponse> retryParse(@PathVariable Long id) {
        documentService.resetForRetry(id);
        return ApiResponse.ok(DocumentResponse.from(documentService.getDocument(id)));
    }

    @GetMapping("/{id}/chunks")
    @PreAuthorize("hasAuthority('knowledge:document:view')")
    public ApiResponse<List<ChunkResponse>> chunks(@PathVariable Long id) {
        documentService.getDocument(id);
        return ApiResponse.ok(chunkMapper.findByDocumentId(id).stream().map(ChunkResponse::from).toList());
    }
}

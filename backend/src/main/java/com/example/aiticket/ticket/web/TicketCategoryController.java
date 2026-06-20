package com.example.aiticket.ticket.web;

import com.example.aiticket.common.api.ApiResponse;
import com.example.aiticket.ticket.service.TicketCategoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ticket-categories")
public class TicketCategoryController {
    private final TicketCategoryService service;

    public TicketCategoryController(TicketCategoryService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ticket:manage')")
    public ApiResponse<List<TicketCategoryResponse>> list(
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return ApiResponse.ok(service.list(includeDisabled).stream()
                .map(TicketCategoryResponse::from)
                .toList());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ticket:manage')")
    public ApiResponse<TicketCategoryResponse> create(@Valid @RequestBody CreateTicketCategoryRequest request) {
        return ApiResponse.ok(TicketCategoryResponse.from(service.create(
                request.name(), request.parentId(), request.sortOrder(), request.enabled())));
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('ticket:manage')")
    public ApiResponse<TicketCategoryResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateTicketCategoryRequest request) {
        return ApiResponse.ok(TicketCategoryResponse.from(service.update(
                id, request.name(), request.parentId(), request.sortOrder())));
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('ticket:manage')")
    public ApiResponse<TicketCategoryResponse> enable(@PathVariable Long id) {
        return ApiResponse.ok(TicketCategoryResponse.from(service.enable(id)));
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('ticket:manage')")
    public ApiResponse<TicketCategoryResponse> disable(@PathVariable Long id) {
        return ApiResponse.ok(TicketCategoryResponse.from(service.disable(id)));
    }
}

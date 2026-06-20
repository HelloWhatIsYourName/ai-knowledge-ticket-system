package com.example.aiticket.ticket.web;

import com.example.aiticket.ticket.domain.TicketCategory;

import java.time.LocalDateTime;

public record TicketCategoryResponse(
        Long id,
        String name,
        Long parentId,
        Integer sortOrder,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TicketCategoryResponse from(TicketCategory category) {
        return new TicketCategoryResponse(
                category.id(),
                category.name(),
                category.parentId(),
                category.sortOrder(),
                category.enabled(),
                category.createdAt(),
                category.updatedAt()
        );
    }
}

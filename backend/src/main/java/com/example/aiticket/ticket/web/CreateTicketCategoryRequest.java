package com.example.aiticket.ticket.web;

import jakarta.validation.constraints.NotBlank;

public record CreateTicketCategoryRequest(
        @NotBlank String name,
        Long parentId,
        Integer sortOrder,
        Boolean enabled
) {
}

package com.example.aiticket.ticket.web;

import jakarta.validation.constraints.NotBlank;

public record UpdateTicketCategoryRequest(
        @NotBlank String name,
        Long parentId,
        Integer sortOrder
) {
}

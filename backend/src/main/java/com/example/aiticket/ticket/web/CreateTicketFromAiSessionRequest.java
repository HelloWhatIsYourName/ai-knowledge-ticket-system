package com.example.aiticket.ticket.web;

import com.example.aiticket.ticket.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketFromAiSessionRequest(
        @NotNull Long sessionId,
        Long assistantMessageId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 5000) String description,
        Long categoryId,
        TicketPriority priority,
        @Size(max = 500) String transferReason
) {
}

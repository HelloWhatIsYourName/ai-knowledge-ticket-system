package com.example.aiticket.ticket.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssignTicketRequest(
        @NotNull Long assigneeId,
        @Size(max = 1000) String comment
) {
}

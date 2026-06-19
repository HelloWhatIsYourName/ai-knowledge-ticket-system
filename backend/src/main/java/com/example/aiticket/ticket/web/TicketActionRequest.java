package com.example.aiticket.ticket.web;

import jakarta.validation.constraints.Size;

public record TicketActionRequest(
        @Size(max = 1000) String comment
) {
}

package com.example.aiticket.system;

public record SystemRoleSummary(
        Long id,
        String roleCode,
        String roleName,
        String dataScope,
        String status,
        Integer sortOrder
) {
}

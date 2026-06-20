package com.example.aiticket.system.web;

import com.example.aiticket.system.SystemRoleSummary;

public record SystemRoleResponse(
        Long id,
        String roleCode,
        String roleName,
        String dataScope,
        String status,
        Integer sortOrder
) {
    public static SystemRoleResponse from(SystemRoleSummary role) {
        return new SystemRoleResponse(role.id(), role.roleCode(), role.roleName(),
                role.dataScope(), role.status(), role.sortOrder());
    }
}

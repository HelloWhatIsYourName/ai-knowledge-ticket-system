package com.example.aiticket.system;

public record SystemPermissionSummary(
        Long id,
        String permissionCode,
        String permissionName,
        String module
) {
}

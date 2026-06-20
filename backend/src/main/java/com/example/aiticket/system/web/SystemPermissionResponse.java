package com.example.aiticket.system.web;

import com.example.aiticket.system.SystemPermissionSummary;

public record SystemPermissionResponse(
        Long id,
        String permissionCode,
        String permissionName,
        String module
) {
    public static SystemPermissionResponse from(SystemPermissionSummary permission) {
        return new SystemPermissionResponse(permission.id(), permission.permissionCode(),
                permission.permissionName(), permission.module());
    }
}

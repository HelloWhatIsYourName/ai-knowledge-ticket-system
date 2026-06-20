package com.example.aiticket.system.web;

import com.example.aiticket.system.SystemUserSummary;

import java.util.List;

public record SystemUserResponse(
        Long id,
        String username,
        String displayName,
        String status,
        List<Long> roleIds
) {
    public static SystemUserResponse from(SystemUserSummary user) {
        return new SystemUserResponse(user.id(), user.username(), user.displayName(), user.status(), user.roleIds());
    }
}

package com.example.aiticket.system;

import org.apache.ibatis.annotations.AutomapConstructor;

import java.util.List;

public record SystemUserSummary(
        Long id,
        String username,
        String displayName,
        String status,
        List<Long> roleIds
) {
    @AutomapConstructor
    public SystemUserSummary(Long id, String username, String displayName, String status) {
        this(id, username, displayName, status, List.of());
    }
}

package com.example.aiticket.auth;

import com.example.aiticket.system.MenuSummary;

import java.util.List;

public record CurrentUserResponse(
        UserSummary user,
        List<String> roles,
        List<String> permissions,
        List<MenuSummary> menus
) {
    public record UserSummary(Long id, String username, String displayName) {
    }
}

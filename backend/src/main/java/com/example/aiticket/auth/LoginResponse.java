package com.example.aiticket.auth;

import com.example.aiticket.system.MenuSummary;

import java.util.List;

public record LoginResponse(
        String tokenType,
        String accessToken,
        long expiresIn,
        CurrentUserResponse.UserSummary user,
        List<String> roles,
        List<String> permissions,
        List<MenuSummary> menus
) {
}

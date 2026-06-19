package com.example.aiticket.security;

import java.util.List;

public record AuthenticatedUser(
        Long id,
        String username,
        String displayName,
        int tokenVersion,
        List<String> roles,
        List<String> permissions
) {
}

package com.example.aiticket.security;

public record JwtClaims(Long userId, String username, int tokenVersion) {
}

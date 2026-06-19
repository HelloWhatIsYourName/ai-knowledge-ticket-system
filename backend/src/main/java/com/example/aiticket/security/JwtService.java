package com.example.aiticket.security;

import com.example.aiticket.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String username, int tokenVersion) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getExpiresInSeconds());
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("tokenVersion", tokenVersion)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public JwtClaims parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Number userId = claims.get("userId", Number.class);
            Number tokenVersion = claims.get("tokenVersion", Number.class);
            return new JwtClaims(userId.longValue(), claims.getSubject(), tokenVersion.intValue());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("invalid jwt token", ex);
        }
    }

    public long expiresInSeconds() {
        return properties.getExpiresInSeconds();
    }
}
